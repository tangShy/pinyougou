package com.pinyougou.page.service;

/**
 * 商品详细页接口
 */
public interface ItemPageService {
    /**
     * 生产商品详细页
     * @param goodsId
     * @return
     */
    boolean genItemHtml(Long goodsId);
}
