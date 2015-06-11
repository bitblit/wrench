package com.erigir.wrench;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Utils for working with java collections
 *
 * NOTE - May replace these with streaming in JDK 8
 */
public class CollectionUtils {

    /*
     * Puts the specified value into the map, only if the value isn't null
     */
    public static <K, V> V putIfNotNull(Map<K, V> map, K key, V value) {
        if (map != null && value != null && key != null) {
            return map.put(key, value);
        } else {
            return null;
        }
    }

    /*
        Creates a new list with the specified item replaced
     */
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

    /*
        Returns a the requested property, or the default if it isnt there
     */
    public static String defaultedGet(Properties props, String name, String def) {
        String rval = null;
        if (props != null) {
            rval = props.getProperty(name);
        }
        return (rval == null) ? def : rval;
    }


}
