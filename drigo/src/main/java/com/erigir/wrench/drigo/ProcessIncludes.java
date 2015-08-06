package com.erigir.wrench.drigo;

/**
 * Created by cweiss on 8/5/15.
 */
public class ProcessIncludes {
    private String includeRegex;
    private String prefix;
    private String suffix;

    public String getIncludeRegex() {
        return includeRegex;
    }

    public void setIncludeRegex(String includeRegex) {
        this.includeRegex = includeRegex;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}
