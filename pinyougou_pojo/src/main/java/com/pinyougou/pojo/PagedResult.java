package com.pinyougou.pojo;

import java.io.Serializable;
import java.util.List;

public class PagedResult<T> implements Serializable {
    private long total;         //代表总记录数
    private List<T> rows;        //代表每页的记录集合

    public PagedResult() {
    }

    public PagedResult(long total, List<T> rows) {
        this.total = total;
        this.rows = rows;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }

    @Override
    public String toString() {
        return "PagedResult{" +
                "total=" + total +
                ", rows=" + rows +
                '}';
    }
}
