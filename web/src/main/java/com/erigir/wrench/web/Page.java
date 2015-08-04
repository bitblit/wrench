package com.erigir.wrench.ape.model;

import java.util.List;

/**
 * Created by chrweiss on 7/7/14.
 */
public class Page<T> {
    private int pageNumber = 0;
    private List<T> items;
    private int maxItemsPerPage;
    private Integer totalItemCount;

    public Page() {
    }

    public Page(int pageNumber, List<T> items, int maxItemsPerPage, Integer totalItemCount) {
        this.pageNumber = pageNumber;
        this.items = items;
        this.maxItemsPerPage = maxItemsPerPage;
        this.totalItemCount = totalItemCount;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public int getMaxItemsPerPage() {
        return maxItemsPerPage;
    }

    public void setMaxItemsPerPage(int maxItemsPerPage) {
        this.maxItemsPerPage = maxItemsPerPage;
    }

    public Integer getTotalItemCount() {
        return totalItemCount;
    }

    public void setTotalItemCount(Integer totalItemCount) {
        this.totalItemCount = totalItemCount;
    }
}
