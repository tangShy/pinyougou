package com.pinyougou.sellergoods.service.impl;

import java.util.List;
import java.util.Map;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.pojo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.sellergoods.service.BrandService;
import org.springframework.util.StringUtils;


@Service
public class BrandServiceImpl implements BrandService {

	@Autowired
	private TbBrandMapper brandMapper;
	@Override
	public List<TbBrand> findAll() {
		return brandMapper.selectByExample(null);
	}

    @Override
    public TbBrand findOne(Long id) {
        return brandMapper.selectByPrimaryKey(id);
    }

    @Override
    public PageResult findPage(int page, int rows) {
        //1.开始分页
        PageHelper.startPage(page,rows);
        //2.查询所有的数据
        TbBrandExample example = new TbBrandExample();
        //3.进行分页
        Page<TbBrand> pages = (Page<TbBrand>) brandMapper.selectByExample(example);
        //4.返回分页结果
        return new PageResult(pages.getTotal(),pages.getResult());
    }

    @Override
    public void add(TbBrand tbBrand) {
        brandMapper.insert(tbBrand);
    }

    @Override
    public void update(TbBrand tbBrand) {
        brandMapper.updateByPrimaryKey(tbBrand);
    }

    @Override
    public void delete(long[] ids) {
        for(long id : ids){
            brandMapper.deleteByPrimaryKey(id);
        }
    }

    @Override
    public PageResult search(int page, int rows, TbBrand tbBrand) {
	    PageHelper.startPage(page,rows);
	    TbBrandExample example = new TbBrandExample();
        TbBrandExample.Criteria criteria = example.createCriteria();
        if(tbBrand != null){
            if(!StringUtils.isEmpty(tbBrand.getName())){
                criteria.andNameLike("%" + tbBrand.getName() + "%");
            }
            if(!StringUtils.isEmpty(tbBrand.getFirstChar())){
                criteria.andFirstCharEqualTo(tbBrand.getFirstChar());
            }
        }
        Page<TbBrand> pages = (Page<TbBrand>) brandMapper.selectByExample(example);

        return new PageResult(pages.getTotal(),pages.getResult());
    }

    @Override
    public List<Map> selectOptionList() {
        return brandMapper.selectOptionList();
    }
}
