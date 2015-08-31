package com.erigir.wrench.sos;

import com.erigir.wrench.ZipUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.Class;import java.lang.Exception;import java.lang.IllegalArgumentException;import java.lang.IllegalStateException;import java.lang.Override;import java.lang.String;import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * User: chrweiss
 * Date: 12/28/13
 * Time: 10:41 PM
 */
public class FileObjectStorageImplementation implements ObjectStorageImplementation {
    private static final Logger LOG = LoggerFactory.getLogger(FileObjectStorageImplementation.class);

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
    public void storeBytes(String fullKey, InputStream zipped) {
        if (zipped != null && fullKey != null) {
            try {
                FileOutputStream fos = new FileOutputStream(fullKey);
                SimpleObjectStorageService.copyStream(zipped, fos);
                fos.close();
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
            File f = new File(fullKey);
            if (f.exists() && f.isFile())
            {
                f.delete();
            }
            else
            {
                LOG.warn("Tried to delete non-existing (or non-file) : {}",f);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Error deleting object " + fullKey, e);
        }
    }

    @Override
    public InputStream readBytes(String fullKey)
            throws IOException {
        InputStream rval = null;
        if (fullKey != null) {
            try {
                rval = new BufferedInputStream(new FileInputStream(fullKey));
            }
            catch (FileNotFoundException fnf)
            {
                LOG.info("Tried to read non-exisiting file : {}",fullKey);
            }
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
