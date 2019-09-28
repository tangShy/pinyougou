package com.pinyougou.pojogroup;

import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;

import java.io.Serializable;
import java.util.List;

public class Goods implements Serializable {
    private TbGoods tbGoods;
    private TbGoodsDesc tbGoodsDesc;
    private List<TbItem> items;

    public Goods(){}

    public Goods(TbGoods tbGoods, TbGoodsDesc tbGoodsDesc) {
        this.tbGoods = tbGoods;
        this.tbGoodsDesc = tbGoodsDesc;
    }

    public TbGoods getTbGoods() {
        return tbGoods;
    }

    public void setTbGoods(TbGoods tbGoods) {
        this.tbGoods = tbGoods;
    }

    public TbGoodsDesc getTbGoodsDesc() {
        return tbGoodsDesc;
    }

    public void setTbGoodsDesc(TbGoodsDesc tbGoodsDesc) {
        this.tbGoodsDesc = tbGoodsDesc;
    }

    public List<TbItem> getItems() {
        return items;
    }

    public void setItems(List<TbItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "Goods{" +
                "tbGoods=" + tbGoods +
                ", tbGoodsDesc=" + tbGoodsDesc +
                ", items=" + items +
                '}';
    }
}
