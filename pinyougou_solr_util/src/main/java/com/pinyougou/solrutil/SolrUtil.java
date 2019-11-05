package com.pinyougou.solrutil;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Component
public class SolrUtil {
    @Autowired
    private TbItemMapper tbItemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
        SolrUtil solrUtil = (SolrUtil)context.getBean("solrUtil");
        solrUtil.importItemData();
        solrUtil.deleteAll();//删除所有缓存
    }

    // 导入商品数据
    public void importItemData() {
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");//已审核
        List<TbItem> itemList = tbItemMapper.selectByExample(example);
        System.out.println("===商品列表===");
        for (TbItem item : itemList) {
            Map specMap = JSON.parseObject(item.getSpec(),Map.class);//将spec字段中的json字符串转换为map
            item.setSpecMap(specMap);//给带注解的字段赋值
            System.out.println(item.getId() + "--" + item.getTitle() + "--" +item.getPrice());
        }
        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
        System.out.println("===结束===");
    }

    //删除所有缓存
    public void deleteAll(){
        Query query = new SimpleQuery("*:*");
        solrTemplate.delete(query);
        solrTemplate.commit();
        System.out.println("删除完成");
    }
}
