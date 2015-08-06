package com.erigir.wrench.drigo;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by cweiss on 8/5/15.
 */
public class DrigoResults {
    public static final String APPLIED_KEY = "APPLIED";
    private DrigoConfiguration sourceConfiguration;
    private Map<File, Map<String, String>> metadata = new TreeMap<>();
    private long startTime = System.currentTimeMillis();
    private long endTime;

    public DrigoConfiguration getSourceConfiguration() {
        return sourceConfiguration;
    }

    public void setSourceConfiguration(DrigoConfiguration sourceConfiguration) {
        this.sourceConfiguration = sourceConfiguration;
    }

    public Map<File, Map<String, String>> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<File, Map<String, String>> metadata) {
        this.metadata = metadata;
    }


    public void addMetadata(File file, String name, String value) {
        Map<String, String> data = metadata.get(file);
        if (data == null) {
            data = new TreeMap<>();
            metadata.put(file, data);
        }
        data.put(name, value);
    }

    public String fetchMetadata(File file, String key) {
        Map<String, String> data = metadata.get(file);
        return (data == null) ? null : data.get(key);
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getElapsedTime()
    {
        return (endTime>0)?(endTime-startTime):System.currentTimeMillis() - startTime;
    }

    public String toString()
    {
        return "[DrigoResults: "+metadata+" in "+getElapsedTime()+" ms]";
    }
}
