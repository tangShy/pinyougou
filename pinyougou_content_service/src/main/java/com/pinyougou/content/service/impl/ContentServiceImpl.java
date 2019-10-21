package com.pinyougou.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.content.service.ContentService;
import com.pinyougou.mapper.TbContentMapper;
import com.pinyougou.pojo.PageResult;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.pojo.TbContentExample;
import com.pinyougou.pojo.TbContentExample.Criteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;

	@Autowired
	private RedisTemplate redisTemplate;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbContent> page=   (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {
		contentMapper.insert(content);
		//清除缓存
		redisTemplate.boundHashOps("tbContent").delete(content.getCategoryId());
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){
		// 查询修改前的分类id,并清除原来的缓存
		Long categoryId = contentMapper.selectByPrimaryKey(content.getId()).getCategoryId();
		redisTemplate.boundHashOps("tbContent").delete(categoryId);
		contentMapper.updateByPrimaryKey(content);
		//如果分类ID发生了修改,清除修改后的分类ID的缓存
		if(categoryId.longValue() != content.getCategoryId().longValue()) {
			redisTemplate.boundHashOps("tbContent").delete(content.getCategoryId());
		}
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//清除缓存
			Long categoryId = contentMapper.selectByPrimaryKey(id).getCategoryId();
			redisTemplate.boundHashOps("tbContent").delete(categoryId);
			contentMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
	@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbContentExample example=new TbContentExample();
		Criteria criteria = example.createCriteria();
		
		if(content!=null){			
						if(content.getTitle()!=null && content.getTitle().length()>0){
				criteria.andTitleLike("%"+content.getTitle()+"%");
			}
			if(content.getUrl()!=null && content.getUrl().length()>0){
				criteria.andUrlLike("%"+content.getUrl()+"%");
			}
			if(content.getPic()!=null && content.getPic().length()>0){
				criteria.andPicLike("%"+content.getPic()+"%");
			}
			if(content.getStatus()!=null && content.getStatus().length()>0){
				criteria.andStatusLike("%"+content.getStatus()+"%");
			}
	
		}
		
		Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<TbContent> findByCategoryId(Long categoryId) {
		List<TbContent> tbContentList = (List<TbContent>) redisTemplate.boundHashOps("tbContent").get(categoryId);
		if (tbContentList == null) {
			System.out.println("从数据库中查询并读取");
			//根据广告分类ID查询广告列表
			TbContentExample contentExample = new TbContentExample();
			Criteria criteria = contentExample.createCriteria();
			criteria.andCategoryIdEqualTo(categoryId);        //类目id
			criteria.andStatusEqualTo("1");            //有效状态
			contentExample.setOrderByClause("sort_order");    //排序
			tbContentList = contentMapper.selectByExample(contentExample);//获取广告列表
			redisTemplate.boundHashOps("tbContent").put(categoryId, tbContentList);//存入缓存
		} else {
			System.out.println("从缓存读取数据");
		}
		return tbContentList;
	}
}
