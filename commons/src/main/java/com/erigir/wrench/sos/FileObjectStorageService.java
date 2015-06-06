package com.erigir.wrench.sos;

import com.erigir.wrench.ZipUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.Class;import java.lang.Exception;import java.lang.IllegalArgumentException;import java.lang.IllegalStateException;import java.lang.Override;import java.lang.String;import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * User: chrweiss
 * Date: 12/28/13
 * Time: 10:41 PM
 */
public class FileObjectStorageService implements ObjectStorageImplementation {
    private static final Logger LOG = LoggerFactory.getLogger(FileObjectStorageService.class);

    private File dir;

    public File toFile(Class clazz, String key) {
        File par = new File(dir, clazz.getName());
        par.mkdirs();
        return new File(par, key);
    }

    @Override
    public String toFullKey(Class clazz, String key) {
        return toFile(clazz,key).getAbsolutePath();
    }

    @Override
    public void storeBytes(String fullKey, byte[] zipped) {
        if (zipped != null && fullKey != null) {
            try {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fullKey));
                bos.write(zipped);
                bos.flush();
                bos.close();
            } catch (Exception e) {
                throw new IllegalArgumentException("Error storing object", e);
            }
        }
    }

    @Override
    public <T> Map<String, StoredObjectMetadata> listObjects(Class<T> type) {
        LOG.info("Fetching all data for class {}", type);
        Long start = System.currentTimeMillis();

        File par = new File(dir, type.getName());

        Map<String, StoredObjectMetadata> rval = new TreeMap<String, StoredObjectMetadata>();
        for (String s : par.list()) {
            File f = new File(par, s);
            StoredObjectMetadata next = new StoredObjectMetadata();
            next.setKey(s);
            next.setModified(new Date(f.lastModified()));
            next.setType(type);
            rval.put(s, next);
        }

        LOG.info("Fetched {} summaries in {} ms", rval.size(), System.currentTimeMillis()-start);

        return rval;
    }

    @Override
    public void deleteObject(String fullKey) {
        try {
            new File(fullKey).delete();
        } catch (Exception e) {
            throw new IllegalStateException("Error deleting object " + fullKey, e);
        }
    }

    @Override
    public byte[] readBytes(String fullKey)
            throws IOException {
        byte[] rval = null;
        if (fullKey != null) {
            return ZipUtils.toByteArray(new FileInputStream(fullKey));
        }
        return rval;
    }

    public void setDir(File dir) {
        this.dir = dir;
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IllegalStateException("Couldnt create home dir : " + dir);
            }
        } else {
            if (dir.isFile()) {
                throw new IllegalStateException("Cant create dir - already is a file");
            }
        }

    }
}
