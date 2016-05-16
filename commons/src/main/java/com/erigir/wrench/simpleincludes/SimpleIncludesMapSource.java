package com.erigir.wrench.simpleincludes;

import java.util.Map;
import java.util.Objects;

/**
 * Created by cweiss on 8/4/15.
 */
public class SimpleIncludesMapSource implements SimpleIncludesSource {
    private Map<String, String> data;

    public SimpleIncludesMapSource() {
    }

    public SimpleIncludesMapSource(Map<String, String> data) {
        this.data = data;
    }

    @Override
    public String findContent(String name) {
        Objects.requireNonNull(data);
        Objects.requireNonNull(name);
        return data.get(name);
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }
}
