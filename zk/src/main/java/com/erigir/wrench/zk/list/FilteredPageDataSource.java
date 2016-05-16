package com.erigir.wrench.zk.list;


import java.util.List;
import java.util.Map;

/**
 * A datasource that takes a filter of type R and returns objects of type T
 * Created by chrweiss on 4/30/15.
 */
public interface FilteredPageDataSource<T, R> {
    /**
     * Returns the number of entities that match the filter
     *
     * @param filter R containing the current filter definition
     * @return int containing the number of matching items
     */
    int getFilteredCount(R filter);


    /**
     * Of the entities that match the filter, starting from startRow read up to
     * rowCount entities.  May return less than rowCount, and even 0, but never more
     * than rowCount rows.  Index 0 in the returned list is element "startRow" in the
     * underlying datasource
     *
     * @param filter         R containing the filter
     * @param sortDefinition Map(String,Boolean) maps the name of the field to sort by, true for asc sort, false for desc
     * @param startRow       int containing the first row to return
     * @param rowCount       int containing the upper bound of rows to fetch
     * @return List containing up to rowCount rows
     */
    List<T> getFilteredPage(R filter, Map<String, Boolean> sortDefinition, int startRow, int rowCount);
}
