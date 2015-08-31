package com.erigir.wrench.sos;

import com.erigir.wrench.ZipUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.Class;import java.lang.Exception;import java.lang.IllegalArgumentException;import java.lang.Object;import java.lang.Override;import java.lang.String;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * This system assumes that you can write local temp files for storage - if not, look for another service.  This ones
 * simple.
 *
 * User: chrweiss
 * Date: 12/28/13
 * Time: 10:41 PM
 */
public class SimpleObjectStorageService {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleObjectStorageService.class);
    private static final int COPY_BUFFER_SIZE=1024*100; // default 100kb buffer

    private ObjectMapper objectMapper;
    private ObjectStorageImplementation objectStorageImplementation;

    private File tempFile()
    {
        try {
            File rval =  File.createTempFile(SimpleObjectStorageService.class.getSimpleName(), "TMP");
            rval.deleteOnExit();
            return rval;
        }
        catch (IOException ioe)
        {
            throw new RuntimeException("Couldn't create temp file",ioe);
        }
    }


    public void storeObject(Object object, String key) {
        if (object != null && key != null) {
            try {
                File temp = tempFile();
                GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(temp));
                objectMapper.writeValue(gos, object);
                gos.close();
                String fullKey = objectStorageImplementation.toFullKey(object.getClass(), key);
                LOG.info("About to copy {} bytes to storage", temp.length());
                objectStorageImplementation.storeBytes(fullKey, new FileInputStream(temp));
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
                GZIPInputStream gis = new GZIPInputStream(objectStorageImplementation.readBytes(fullKey));
                rval = objectMapper.readValue(gis, type);
                gis.close();
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

    // Normally Id use IOUtils, but didn't want to add a dependency
    public static void copyStream(InputStream in, OutputStream out)
        throws IOException
    {
        byte[] buffer = new byte[COPY_BUFFER_SIZE];
        int len = in.read(buffer);
        while (len != -1) {
            out.write(buffer, 0, len);
            len = in.read(buffer);
        }
    }

}
