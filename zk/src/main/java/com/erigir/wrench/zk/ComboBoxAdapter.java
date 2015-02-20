package com.erigir.wrench.zk;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * An adapter class to easily put a Map into a combo box and allow selection
 * Created by chrweiss on 12/11/14.
 */
public class ComboBoxAdapter<T> {
    private LinkedHashMap<String,T> data;

    public ComboBoxAdapter(Map<String,T> values)
    {
        data = new LinkedHashMap<String,T>();
        data.putAll(values);
    }

    public Set<String> getValues()
    {
        return data.keySet();
    }

    public T getSelectedValue(String key)
    {
        return data.get(key);
    }




}
