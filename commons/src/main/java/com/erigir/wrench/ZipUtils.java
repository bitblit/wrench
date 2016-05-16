package com.erigir.wrench;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.zip.*;

/**
 * cweiss : 1/23/12 6:08 PM
 */
public class ZipUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ZipUtils.class);
    private static final String ZIP_FILE_NAME = "data.dat";

    public static byte[] zipData(byte[] input) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gz = new GZIPOutputStream(baos);
            gz.write(input);
            gz.finish();
            //baos.flush();
            baos.close();
            byte[] data = baos.toByteArray();
            LOG.trace("Compressed {} to {}", input.length, data.length);
            return data;
        } catch (IOException ioe) {
            throw new RuntimeException("Error processing to zip", ioe);
        }
    }

    public static byte[] unzipData(byte[] input) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(input);
            GZIPInputStream gz = new GZIPInputStream(bais);
            byte[] data = toByteArray(gz);
            LOG.trace("Decompressed {} to {}", input.length, data.length);
            return data;
        } catch (IOException ioe) {
            throw new RuntimeException("Error processing from zip", ioe);
        }
    }


    public static byte[] oldZipData(byte[] input) {
        Map<String, byte[]> m = new TreeMap<String, byte[]>();
        m.put(ZIP_FILE_NAME, input);
        return createZip(m);
    }

    public static byte[] oldUnzipData(byte[] input) {
        return extractZipFileIntoMemory(input).get(ZIP_FILE_NAME);
    }


    public static Map<String, byte[]> extractZipFileIntoMemory(byte[] zipFile) {
        Map<String, byte[]> rval = new HashMap<String, byte[]>();
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(zipFile);
            ZipInputStream zis = new ZipInputStream(bais);
            ZipEntry entry;

            //
            // Read each entry from the ZipInputStream until no more entry found
            // indicated by a null return value of the getNextEntry() method.
            //
            while ((entry = zis.getNextEntry()) != null) {
                byte[] d = toByteArray(zis);
                rval.put(entry.getName(), d);
            }

            zis.close();
            bais.close();
        } catch (IOException e) {
            LOG.error("Error reading zip file:" + e, e);
        }
        return rval;
    }

    /**
     * Given a map of filenames to byte arrays (containing files), creates a zip
     * file of the data.
     *
     * @param files Map of files to zip
     * @return byte[] containing the zip file
     */
    public static byte[] createZip(Map<String, byte[]> files) {
        try {
            long totalFileSize = 0;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ZipOutputStream zipfile = new ZipOutputStream(bos);
            Iterator<String> i = files.keySet().iterator();
            String fileName = null;
            ZipEntry zipentry = null;
            while (i.hasNext()) {
                fileName = (String) i.next();
                zipentry = new ZipEntry(fileName);
                zipfile.putNextEntry(zipentry);
                byte[] data = files.get(fileName);
                totalFileSize += data.length;
                zipfile.write(data);
            }
            zipfile.close();
            byte[] rval = bos.toByteArray();
            LOG.debug("Compressed " + totalFileSize + " to " + rval.length + " bytes");
            return rval;
        } catch (IOException ioe) {
            throw new IllegalArgumentException("Cant create zip", ioe);
        }
    }

    public static byte[] toByteArray(InputStream is) {
        Objects.requireNonNull(is, "Cannot pass a null input stream");
        try {
            BufferedInputStream bis = null;
            if (BufferedInputStream.class.isAssignableFrom(is.getClass())) {
                bis = (BufferedInputStream) is;
            } else {
                bis = new BufferedInputStream(is);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int read = bis.read();
            while (read != -1) {
                baos.write(read);
                read = bis.read();
            }
            return baos.toByteArray();
        } catch (IOException ioe) {
            throw new RuntimeException("Error processing input stream", ioe);
        }
    }


}
