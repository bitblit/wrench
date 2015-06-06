package com.erigir.wrench.sos;

import java.util.Date;

/**
 * User: chrweiss
 * Date: 12/28/13
 * Time: 10:46 PM
 */
public class StoredObjectMetadata {
    private Class type;
    private String key;
    private Date modified;

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }
}
