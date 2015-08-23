package com.erigir.wrench.drigo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Copyright 2014 Christopher Weiss
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
public class FileProcessorUtils {
    private static final Logger LOG = LoggerFactory.getLogger(FileProcessorUtils.class);

    public static void copyFolder(File src, File dest, List<Exclusion> exclusionList)
            throws DrigoException {

        try {

            if (Exclusion.excluded(exclusionList, src)) {
                LOG.info("Skipping {} - it is on the exclusion list", src);
            } else {
                if (src.isDirectory()) {

                    //if directory not exists, create it
                    if (!dest.exists()) {
                        dest.mkdir();
                        LOG.info("Directory copied from {} to {}", src, dest);
                    }

                    //list all the directory contents
                    String files[] = src.list();

                    for (String file : files) {
                        //construct the src and dest file structure
                        File srcFile = new File(src, file);
                        File destFile = new File(dest, file);
                        //recursive copy
                        copyFolder(srcFile, destFile, exclusionList);
                    }

                } else {
                    //if file, then copy it
                    //Use bytes stream to support all file types
                    InputStream in = new FileInputStream(src);
                    OutputStream out = new FileOutputStream(dest);

                    byte[] buffer = new byte[1024];

                    int length;
                    //copy the file content in bytes
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }

                    in.close();
                    out.close();
                }
            }
        } catch (IOException ioe) {
            throw new DrigoException("Error cloning file/directory", ioe);
        }
    }

    public static boolean matches(File f, String regex) {
        Pattern p = Pattern.compile(regex);
        boolean rval = (p.matcher(f.getAbsolutePath()).matches());

        LOG.debug("Tested {} against {} returning {}", f.getName(), regex, rval);
        return rval;
    }

}
