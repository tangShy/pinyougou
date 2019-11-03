package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper tbGoodsDescMapper;

    @Autowired
    private TbItemMapper tbItemMapper;

    @Autowired
    TbBrandMapper brandMapper;

    @Autowired
    TbSellerMapper sellerMapper;


    /**
     * 查询全部
     */
    @Override
    public List<TbGoods> findAll() {
        return goodsMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(Goods goods) {
        //1.先在 TbGoods 表中添加数据
        goodsMapper.insert(goods.getTbGoods());
        //2.再在 tbGoodsDesc 表中添加数据
        goods.getTbGoodsDesc().setGoodsId(goods.getTbGoods().getId());
        tbGoodsDescMapper.insert(goods.getTbGoodsDesc());
        //3.向 item 表中添加数据
        //3.1)得到选项列表

        //插入SKU商品信息列表
        saveItemList(goods);
    }

    private void setItemValues(TbItem tbItem, Goods goods) {
        //得到商品 id
        tbItem.setGoodsId(goods.getTbGoods().getId());
        //找到 goods 表中的三级分类赋值给 item 的分类
        tbItem.setCategoryid(goods.getTbGoods().getCategory3Id());
        //得到商家编号
        tbItem.setSellerId(goods.getTbGoods().getSellerId());
        tbItem.setUpdateTime(new Date());
        tbItem.setCreateTime(new Date());

        //根据品牌id查询品牌
        TbBrand brand = brandMapper.selectByPrimaryKey(goods.getTbGoods().getBrandId());
        if (brand == null) {
            throw new RuntimeException("brand is null");
        }
        tbItem.setBrand(brand.getName());
        //根据商家id查询商家（店铺名称）
        TbSeller tbSeller = sellerMapper.selectByPrimaryKey(goods.getTbGoods().getSellerId());
        tbItem.setSeller(tbSeller.getNickName());
        List<Map> imageMaps = JSON.parseArray(goods.getTbGoodsDesc().getItemImages(), Map.class);
        if (imageMaps.size() > 0) {
            tbItem.setImage(imageMaps.get(0).get("url") + "");
        }
    }


    /**
     * 修改
     */
    @Override
    public void update(Goods goods) {
        //1.修改商品列表
        goodsMapper.updateByPrimaryKey(goods.getTbGoods());
        //2.修改商品扩展表
        tbGoodsDescMapper.updateByPrimaryKey(goods.getTbGoodsDesc());
        //3.先删除tbItem表，根据goodsId删除
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdEqualTo(goods.getTbGoods().getId());
        tbItemMapper.deleteByExample(example);
        //4.添加
        saveItemList(goods);
    }

    //添加SKU商品的方法
    private void saveItemList(Goods goods){
        if ("1".equals(goods.getTbGoods().getIsEnableSpec())) {
            List<TbItem> tbItems = goods.getItems();
            System.out.println("tbItems :" + tbItems.size());
            //3.2)遍历选项列表
            for (TbItem tbItem : tbItems) {
                //得到商品名称
                String title = tbItem.getTitle();
                //取得勾选的规格列表
                Map specMap = JSON.parseObject(tbItem.getSpec());
                //遍历map集合
                for (Object key : specMap.keySet()) {
                    //重新构造标题
                    title = title + " " + specMap.get(key);
                    tbItem.setTitle(title);
                    setItemValues(tbItem, goods);
                }
                tbItemMapper.insert(tbItem);
            }
        } else {//没有启用规格
            TbItem tbItem = new TbItem();
            tbItem.setTitle(goods.getTbGoods().getGoodsName());//标题
            tbItem.setPrice(goods.getTbGoods().getPrice());//价格
            tbItem.setNum(9999);//库存数量
            tbItem.setStatus("1");//状态
            tbItem.setIsDefault("1");//默认
            tbItem.setSpec("{}");
            setItemValues(tbItem, goods);
        }
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
//    public TbGoods findOne(Long id) {
    public Goods findOne(Long id) {
        Goods goods = new Goods();
        //1.为goods对象添加tbGoods
        TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
        goods.setTbGoods(tbGoods);
        //2.为goods对象添加tbGoodsDesc
        TbGoodsDesc tbGoodsDesc = tbGoodsDescMapper.selectByPrimaryKey(id);
        goods.setTbGoodsDesc(tbGoodsDesc);
        //3.查询goods相关联的itemList集合对象
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdEqualTo(id);
        List<TbItem> items = tbItemMapper.selectByExample(example);
        goods.setItems(items);
        return goods;
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
//            goodsMapper.deleteByPrimaryKey(id);
            //不适用物理删除，此处删除只改变isDelete状态
            //1.根据id查询商品
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            //2.修改商品状态
            tbGoods.setIsDelete("1");//表示逻辑删除
            //3.修改商品
            goodsMapper.updateByPrimaryKey(tbGoods);
        }
    }


    @Override
    public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbGoodsExample example = new TbGoodsExample();
        Criteria criteria = example.createCriteria();
        //指定条件为未删除的
        criteria.andIsDeleteIsNull();

        if (goods != null) {
            if (goods.getSellerId() != null && goods.getSellerId().length() > 0) {
//                criteria.andSellerIdLike("%" + goods.getSellerId() + "%");
                //改为精确匹配查询
                criteria.andSellerIdEqualTo(goods.getSellerId());
            }
            if (goods.getGoodsName() != null && goods.getGoodsName().length() > 0) {
                criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
            }
            if (goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0) {
                criteria.andAuditStatusLike("%" + goods.getAuditStatus() + "%");
            }
            if (goods.getIsMarketable() != null && goods.getIsMarketable().length() > 0) {
                criteria.andIsMarketableLike("%" + goods.getIsMarketable() + "%");
            }
            if (goods.getCaption() != null && goods.getCaption().length() > 0) {
                criteria.andCaptionLike("%" + goods.getCaption() + "%");
            }
            if (goods.getSmallPic() != null && goods.getSmallPic().length() > 0) {
                criteria.andSmallPicLike("%" + goods.getSmallPic() + "%");
            }
            if (goods.getIsEnableSpec() != null && goods.getIsEnableSpec().length() > 0) {
                criteria.andIsEnableSpecLike("%" + goods.getIsEnableSpec() + "%");
            }
            if (goods.getIsDelete() != null && goods.getIsDelete().length() > 0) {
                criteria.andIsDeleteLike("%" + goods.getIsDelete() + "%");
            }

        }

        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 商品审核，修改状态
     * @param ids
     * @param status
     */
    @Override
    public void updateStatus(Long[] ids, String status) {
        for (Long id : ids) {
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            tbGoods.setAuditStatus(status);
            goodsMapper.updateByPrimaryKey(tbGoods);
        }
    }

    @Override
    public List<TbItem> findItemListByGoodsIdandStatus(Long[] goodsIds, String status) {
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdIn(Arrays.asList(goodsIds));
        criteria.andStatusEqualTo(status);
        return tbItemMapper.selectByExample(example);
    }
}
