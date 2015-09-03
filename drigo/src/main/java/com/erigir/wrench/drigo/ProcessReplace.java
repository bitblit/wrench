package com.erigir.wrench.drigo;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by cweiss on 8/5/15.
 */
public class ProcessReplace {
    private String includeRegex;
    private String prefix;
    private String suffix;
    private LinkedHashMap<Pattern,String> replace;

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

    public LinkedHashMap<Pattern, String> getReplace() {
        return replace;
    }

    public void setReplace(LinkedHashMap<Pattern, String> replace) {
        this.replace = replace;
    }
}
