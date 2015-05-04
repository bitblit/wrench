package com.erigir.wrench.zk.list;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zul.AbstractListModel;
import org.zkoss.zul.FieldComparator;
import org.zkoss.zul.event.ListDataEvent;
import org.zkoss.zul.ext.Sortable;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;


/**
 * A filtered, sorted model that implements page fetch and cache
 * The cache uses a weak hash map to help with memory usage in large cases
 *
 * T is the type that you'll generate a list of,
 * R is the type that will hold your filter
 *
 * Created by chrweiss on 4/30/15.
 */
public class FilteredDynamicListModel<T,R> extends AbstractListModel<T> implements Sortable<T> {
    private static final Logger LOG = LoggerFactory.getLogger(FilteredDynamicListModel.class);

    private R filter;
    private FilteredPageDataSource<T,R> dataSource;
    private Map<String,Boolean> sortDefinition = new LinkedHashMap<>();
    private WeakHashMap<Integer, T> cache = new WeakHashMap<>();
    /**
     * If this is true, only one field is sorted on at a time (setting a new one clears the old one)
     */
    private boolean singleSortMode = true;
    /**
     * How many rows to fetch from the underlying datasource at a time - has nothing to do with
     * how many show up on the screen since this isnt a UI number, but really should be >= average
     * screen page size so we aren't making multiple calls to the DB to show a single page
     */
    private int fetchPageSize;

    public FilteredDynamicListModel(FilteredPageDataSource<T,R> dataSource, R filter)
    {
        super();
        this.filter = filter;
        this.dataSource = dataSource;
        this.fetchPageSize = 30;
    }

    public FilteredDynamicListModel(FilteredPageDataSource<T, R> dataSource, R filter, int fetchPageSize) {
        this.filter = filter;
        this.dataSource = dataSource;
        this.fetchPageSize = fetchPageSize;
    }

    @Override
    public void sort(Comparator<T> comparator, boolean b) {
        LOG.info("Asked to sort {} {}",comparator, b);
        addSort(((FieldComparator) comparator).getRawOrderBy(), b);
        forceRedraw();
    }

    @Override
    public String getSortDirection(Comparator<T> comparator) {
        LOG.info("called getsort {}", comparator);
        Boolean tmp = sortDefinition.get(((FieldComparator) comparator).getRawOrderBy());
        String rval = "natural";
        if (tmp!=null)
        {
            rval = (tmp)?"ascending":"descending";
        }
        return rval;
    }

    @Override
    public int getSize() {
        int rval = dataSource.getFilteredCount(filter);
        return rval;
    }

    @Override
    public T getElementAt(int index) {

        T rval = cache.get(index);
        if (rval==null)
        {
            int startIdx = (index/fetchPageSize)*fetchPageSize;

            LOG.debug("Cache miss at index {} - loading page from {} to {}",index,startIdx,startIdx+fetchPageSize);
            List<T> temp = dataSource.getFilteredPage(filter, sortDefinition, startIdx, fetchPageSize);

            // Copy data into cache
            for (int i=0;i<temp.size();i++)
            {
                cache.put(startIdx+i, temp.get(i));
                if ((startIdx+i)==index)
                {
                    rval = temp.get(i);
                }
            }
        }

        return rval;
    }

    public void setFilter(R filter) {
        LOG.debug("Changed filter to {} - repaging", filter);
        this.filter = filter;
        forceRedraw();
    }

    private void forceRedraw()
    {
        cache = new WeakHashMap<>();
        fireEvent(ListDataEvent.CONTENTS_CHANGED, 0, getSize());
    }

    /**
     * This is a helper function that generates a typical SQL order by clause from the sort map
     * @param input Map(String,Boolean) containing the sort defintion
     * @return A string containing a order by clause such as " order by col1 ASC, col2 DESC"
     */
    public static String buildOrderByClause(Map<String, Boolean> input)
    {
        StringBuilder order = new StringBuilder();

        if (input.size()>0)
        {
            for (Map.Entry<String,Boolean> e:input.entrySet())
            {
                if (order.length()>0)
                {
                    order.append(", ");
                }
                order.append(e.getKey());
                order.append((e.getValue())?" ASC":" DESC");
            }
            order.insert(0," order by "); // so the comma logic works
        }

        String rval = order.toString();
        return rval;
    }

    /**
     * Add a sort column
     * if singleSortMode is true this also clears any existing sorts
     * @param name
     * @param asc
     */
    public void addSort(String name, boolean asc)
    {
        if (singleSortMode)
        {
            sortDefinition = new LinkedHashMap<>();
        }
        sortDefinition.put(name, asc);
    }


    public boolean isSingleSortMode() {
        return singleSortMode;
    }

    public void setSingleSortMode(boolean singleSortMode) {
        this.singleSortMode = singleSortMode;
    }
}
