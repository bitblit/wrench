package com.erigir.wrench.simpleincludes;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Datasource that matches patterns on the RHS instead of exact match strings
 * Force the linked hashmap so we can guarantee ordering
 * Created by cweiss on 8/4/15.
 */
public class SimpleIncludesPatternMapSource implements SimpleIncludesSource {
    private LinkedHashMap<Pattern,String> data;

    public SimpleIncludesPatternMapSource() {
    }

    public SimpleIncludesPatternMapSource(LinkedHashMap<Pattern, String> data) {
        this.data = data;
    }

    @Override
    public String findContent(String name) {
        Objects.requireNonNull(data);
        Objects.requireNonNull(name);

        String rval = null;

        for (Iterator<Map.Entry<Pattern, String>> i =data.entrySet().iterator();i.hasNext() && rval==null;)
        {
            Map.Entry<Pattern, String> e = i.next();
            if (e.getKey().matcher(name).matches())
            {
                rval = e.getValue();
            }
        }

        rval = (rval==null)?"":rval;
        return rval;
    }

    public void setData(LinkedHashMap<Pattern, String> data) {
        this.data = data;
    }
}
