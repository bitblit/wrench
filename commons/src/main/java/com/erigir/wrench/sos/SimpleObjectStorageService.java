package com.erigir.wrench.sos;

import com.erigir.wrench.ZipUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;import java.lang.Class;import java.lang.Exception;import java.lang.IllegalArgumentException;import java.lang.Object;import java.lang.Override;import java.lang.String;
import java.util.Map;

/**
 * User: chrweiss
 * Date: 12/28/13
 * Time: 10:41 PM
 */
public class SimpleObjectStorageService {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleObjectStorageService.class);

    private ObjectMapper objectMapper;
    private ObjectStorageImplementation objectStorageImplementation;

    public void storeObject(Object object, String key) {
        if (object != null && key != null) {
            try {
                String data = objectMapper.writeValueAsString(object);
                byte[] zipped = ZipUtils.zipData(data.getBytes());

                String fullKey = objectStorageImplementation.toFullKey(object.getClass(), key);
                LOG.debug("Object {} compressed {} to {}", new Object[]{fullKey, data.length(), zipped});
                objectStorageImplementation.storeBytes(fullKey, zipped);
            } catch (Exception e) {
                throw new IllegalArgumentException("Error storing object", e);
            }
        }
    }

    public <T> T loadObject(Class<T> type, String key) {
        T rval = null;

        if (type != null && key != null) {
            try {
                String fullKey = objectStorageImplementation.toFullKey(type, key);

                byte[] zipped = objectStorageImplementation.readBytes(fullKey);
                if (zipped != null) {
                    byte[] unzipped = ZipUtils.unzipData(zipped);
                    rval = objectMapper.readValue(unzipped, type);
                }
            } catch (Exception e) {
                LOG.warn("Error loading object", e);
                rval = null;
            }
        }
        return rval;
    }

    public <T> Map<String, StoredObjectMetadata> listObjects(Class<T> type)
    {
        return objectStorageImplementation.listObjects(type);
    }

    public <T> void deleteObject(Class<T> type, String key)
    {
        objectStorageImplementation.deleteObject(objectStorageImplementation.toFullKey(type,key));
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void setObjectStorageImplementation(ObjectStorageImplementation objectStorageImplementation) {
        this.objectStorageImplementation = objectStorageImplementation;
    }
}
