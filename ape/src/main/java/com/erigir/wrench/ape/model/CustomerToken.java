package com.erigir.wrench.ape.model;

import java.util.Map;

/**
 * Created by cweiss on 7/19/15.
 */
public class CustomerToken {
    private String key;
    private Long expires;
    private Long created = System.currentTimeMillis();
    private Map<String, Object> otherData;

    public CustomerToken() {
    }

    public CustomerToken(String key, Long expires) {
        this.key = key;
        this.expires = expires;
    }

    public CustomerToken(String key, Long expires, Map<String, Object> otherData) {
        this.key = key;
        this.expires = expires;
        this.otherData = otherData;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Long getExpires() {
        return expires;
    }

    public void setExpires(Long expires) {
        this.expires = expires;
    }

    public Map<String, Object> getOtherData() {
        return otherData;
    }

    public void setOtherData(Map<String, Object> otherData) {
        this.otherData = otherData;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }
}
