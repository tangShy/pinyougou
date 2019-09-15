package com.pinyougou.sellergoods.service;

import java.util.List;


import com.pinyougou.pojo.PagedResult;
import com.pinyougou.pojo.Result;
import com.pinyougou.pojo.TbBrand;

public interface BrandService {
	//查询所有的品牌
	List<TbBrand> findAll() ;

	PagedResult findPage(int page, int rows);

	void add(TbBrand tbBrand);

    void update(TbBrand tbBrand);

	void delete(long[] ids);

	PagedResult search(int page, int rows, TbBrand tbBrand);
}
