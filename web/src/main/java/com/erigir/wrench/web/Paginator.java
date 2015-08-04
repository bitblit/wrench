package com.erigir.wrench.web;

import com.erigir.wrench.ape.model.Page;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Created by chrweiss on 7/7/14.
 */
public class Paginator {
    private int pageSize;

    public <T> Page<T> pagination(List<T> allItems, Integer inPage) {
        Assert.notNull(allItems, "Items may not be null");
        int page = (inPage == null) ? 0 : inPage;
        int maxItems = allItems.size();

        if (maxItems == 0) {
            return new Page(0, allItems, pageSize, 0);
        } else {
            int pageCount = (int) Math.ceil((double) maxItems / (double) pageSize);
            page = (page < pageCount) ? page : pageCount - 1;

            int start = page * pageSize;
            int end = Math.min(start + pageSize, maxItems);

            return new Page(page, allItems.subList(start, end), pageSize, maxItems);
        }
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
