package com.erigir.wrench.sos;

import java.io.InputStream;
import java.util.Map;

/**
 * An object storage service typically just writes an object+metadata to a store or
 * <p>
 * User: chrweiss
 * Date: 12/28/13
 * Time: 10:38 PM
 */
public interface ObjectStorageImplementation {

    String toFullKey(Class clazz, String key);

    void storeBytes(String fullKey, InputStream bytes)
            throws Exception;

    InputStream readBytes(String fullKey)
            throws Exception;

    <T> Map<String, StoredObjectMetadata> listObjects(Class<T> type);

    void deleteObject(String fullKey);


}
