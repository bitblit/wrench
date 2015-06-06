package com.erigir.wrench.sos;

import java.util.List;
import java.util.Map;

/**
 * An object storage service typically just writes an object+metadata to a store or
 *
 * User: chrweiss
 * Date: 12/28/13
 * Time: 10:38 PM
 */
public interface ObjectStorageImplementation {

    String toFullKey(Class clazz, String key);

    void storeBytes(String fullKey, byte[] bytes)
            throws Exception;

    byte[] readBytes(String fullKey)
            throws Exception;

    <T> Map<String, StoredObjectMetadata> listObjects(Class<T> type);

    void deleteObject(String fullKey);


}
