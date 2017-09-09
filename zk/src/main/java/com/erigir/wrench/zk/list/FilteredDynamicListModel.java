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


/**
 * A filtered, sorted model that implements page fetch and cache
 * The cache uses a weak hash map to help with memory usage in large cases
 * <p>
 * T is the type that you'll generate a list of,
 * R is the type that will hold your filter
 * <p>
 * Created by chrweiss on 4/30/15.
 */
public class FilteredDynamicListModel<T, R> extends AbstractListModel<T> implements Sortable<T> {
  private static final Logger LOG = LoggerFactory.getLogger(FilteredDynamicListModel.class);

  private R filter;
  private FilteredPageDataSource<T, R> dataSource;
  private Map<String, Boolean> sortDefinition = new LinkedHashMap<>();

  private Integer offset;
  /**
   * This is here since ZK still reserves memory for the whole list for some reason - if this is set,
   * and the actual number of items is larger than it, then getSize() will return this, and
   * getRealCount will return the real count of items (you can use that to display a "results too large"
   * message on screen if you like).  Hopefully ZK can make this work (or I can figure out how I'm doing
   * Rows on Demand wrong) to make this unnecessary
   */
  private Integer limitingMaximum = null;
  private Integer realCount = null;

  //private WeakHashMap<Integer, T> cache = new WeakHashMap<>();
  private T[] cache;
  private Integer cacheSize = null;
  /**
   * If this is true, only one field is sorted on at a time (setting a new one clears the old one)
   */
  private boolean singleSortMode = true;
  /**
   * How many rows to fetch from the underlying datasource at a time - has nothing to do with
   * how many show up on the screen since this isnt a UI number, but really should be &gt;= average
   * screen page size so we aren't making multiple calls to the DB to show a single page
   */
  private int fetchPageSize;

  public FilteredDynamicListModel(FilteredPageDataSource<T, R> dataSource, R filter) {
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

  /**
   * This is a helper function that generates a typical SQL order by clause from the sort map
   *
   * @param input Map(String,Boolean) containing the sort defintion
   * @return A string containing a order by clause such as " order by col1 ASC, col2 DESC"
   */
  public static String buildOrderByClause(Map<String, Boolean> input) {
    StringBuilder order = new StringBuilder();

    if (input.size() > 0) {
      for (Map.Entry<String, Boolean> e : input.entrySet()) {
        if (order.length() > 0) {
          order.append(", ");
        }
        order.append(e.getKey());
        order.append((e.getValue()) ? " ASC" : " DESC");
      }
      order.insert(0, " order by "); // so the comma logic works
    }

    String rval = order.toString();
    return rval;
  }

  @Override
  public void sort(Comparator<T> comparator, boolean b) {
    LOG.info("Asked to sort {} {}", comparator, b);
    addSort(((FieldComparator) comparator).getRawOrderBy(), b);
    forceRedraw();
  }

  @Override
  public String getSortDirection(Comparator<T> comparator) {
    LOG.info("called getsort {}", comparator);
    Boolean tmp = sortDefinition.get(((FieldComparator) comparator).getRawOrderBy());
    String rval = "natural";
    if (tmp != null) {
      rval = (tmp) ? "ascending" : "descending";
    }
    return rval;
  }

  @Override
  public int getSize() {
    if (cacheSize == null) {
      int temp = dataSource.getFilteredCount(filter);
      if (limitingMaximum == null || temp < limitingMaximum) {
        cacheSize = temp;
        realCount = null;
      } else {
        LOG.debug("Actual size is {} but limiting to {}", temp, limitingMaximum);
        cacheSize = limitingMaximum;
        realCount = temp;
      }
    }
    return cacheSize;
  }

  @Override
  public T getElementAt(int index) {

    if (cache == null || offset == null || index < offset || index >= offset + cache.length) {
      // Index is outside of cache boundaries.  Fetch cache and move boundaries
      int startIdx = (index / fetchPageSize) * fetchPageSize;

      LOG.debug("Cache miss at index {} - loading page from {} to {}", index, startIdx, startIdx + fetchPageSize);
      List<T> temp = dataSource.getFilteredPage(filter, sortDefinition, startIdx, fetchPageSize);

      cache = (T[]) temp.toArray(new Object[0]);
      offset = startIdx;

      LOG.debug("Finished fetch, offset is now {}", startIdx);

    }

    int toFetch = index - offset;
    return (toFetch < cache.length) ? cache[toFetch] : null;
  }

  public void setFilter(R filter) {
    LOG.debug("Changed filter to {} - repaging", filter);
    this.filter = filter;
    forceRedraw();
  }

  public void forceRedraw() {
    cacheSize = null;
    cache = null;
    fireEvent(ListDataEvent.CONTENTS_CHANGED, 0, getSize());
  }

  /**
   * Add a sort column
   * if singleSortMode is true this also clears any existing sorts
   *
   * @param name String containing the name of the column to sort by
   * @param asc  boolean containing true if ascending sort, false for descending
   */
  public void addSort(String name, boolean asc) {
    if (singleSortMode) {
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

  public Integer getLimitingMaximum() {
    return limitingMaximum;
  }

  public void setLimitingMaximum(Integer limitingMaximum) {
    this.limitingMaximum = limitingMaximum;
  }

  public Integer getRealCount() {
    return realCount;
  }
}
