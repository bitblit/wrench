package com.erigir.wrench.sos;

import com.erigir.wrench.QuietObjectMapper;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;

import java.io.File;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Created by chrweiss on 6/9/15.
 */
public class TestFileObjectStorageImplementation {
    private static final Logger LOG = LoggerFactory.getLogger(TestFileObjectStorageImplementation.class);

    @Test
    public void testSimpleStorage()
    {
        FileObjectStorageImplementation fos = new FileObjectStorageImplementation();
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        LOG.info("Using tmp dir {}", tempDir);
        fos.setDir(tempDir);

        TreeMap<String,String> toStore = new TreeMap<>();
        toStore.put("tk-1", "tv-1");
        toStore.put("tk-2", "tv-2");

        SimpleObjectStorageService sos = new SimpleObjectStorageService();
        sos.setObjectMapper(new QuietObjectMapper());
        sos.setObjectStorageImplementation(fos);

        String key = "my-test-key";
        sos.storeObject(toStore, key);

        Map<String,String> load = sos.loadObject(TreeMap.class, key);

        assertNotNull(load);
        assertEquals(load.size(), toStore.size());

        for(Map.Entry<String,String> e:toStore.entrySet())
        {
            assertEquals(load.get(e.getKey()), e.getValue());
        }

        sos.deleteObject(TreeMap.class, key);

        Map<String,String> load2 = sos.loadObject(TreeMap.class,key);

        assertNull(load2);

    }


}
