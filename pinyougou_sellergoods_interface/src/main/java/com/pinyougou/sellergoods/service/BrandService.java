package com.pinyougou.sellergoods.service;

import java.util.List;
import java.util.Map;


import com.pinyougou.pojo.PageResult;
import com.pinyougou.pojo.TbBrand;

public interface BrandService {
	//查询所有的品牌
	List<TbBrand> findAll() ;

	PageResult findPage(int page, int rows);

	void add(TbBrand tbBrand);

    void update(TbBrand tbBrand);

	void delete(long[] ids);

	PageResult search(int page, int rows, TbBrand tbBrand);

	TbBrand findOne(Long id);

	//品牌下拉框数据
	List<Map> selectOptionList();
}
