package com.erigir.wrench;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Utils for working with java collections
 */
public class CollectionUtils {

    /**
     * Puts the specified value into the map, only if it isn't null
     *
     * @param map
     * @param key
     * @param value
     * @param <K>
     * @param <V>
     */
    public static <K, V> V putIfNotNull(Map<K, V> map, K key, V value) {
        if (map != null && value != null && key != null) {
            return map.put(key, value);
        } else {
            return null;
        }
    }

    public static <T> List replaceItemInUnmodifiableList(List<T> list, T newOb, int idx) {
        List rval = new ArrayList(list.size());
        for (int i = 0; i < list.size(); i++) {
            if (i != idx) {
                rval.add(list.get(i));
            } else {
                rval.add(newOb);
            }
        }
        return rval;
    }

    public static String defaultedGet(Properties props, String name, String def) {
        String rval = null;
        if (props != null) {
            rval = props.getProperty(name);
        }
        return (rval == null) ? def : rval;
    }


}
