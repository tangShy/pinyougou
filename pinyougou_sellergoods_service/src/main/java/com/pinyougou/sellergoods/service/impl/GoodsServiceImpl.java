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

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
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
    public void update(TbGoods goods) {
        goodsMapper.updateByPrimaryKey(goods);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbGoods findOne(Long id) {
        return goodsMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            goodsMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbGoodsExample example = new TbGoodsExample();
        Criteria criteria = example.createCriteria();

        if (goods != null) {
            if (goods.getSellerId() != null && goods.getSellerId().length() > 0) {
                criteria.andSellerIdLike("%" + goods.getSellerId() + "%");
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

}
