package com.erigir.wrench.drigo.processor;

import com.erigir.wrench.drigo.DrigoException;
import com.erigir.wrench.drigo.DrigoResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

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
public abstract class AbstractFileProcessor implements FileProcessor {

    public boolean process(File src, DrigoResults results) {
        log().info("Applying {} to {}", getClass().getSimpleName(), src.getName());

        try {
            if (!src.exists() || !src.isFile()) {
                throw new DrigoException(src + " doesnt exist or isnt a file");
            }

            File tmp = File.createTempFile("tst", "tst");

            boolean rval = innerProcess(src, tmp, results);


            if (rval) {
                logDeltaIfExists(src, tmp);
                src.delete();
                tmp.renameTo(src);
            } else {
                tmp.delete();
            }

            return rval;
        } catch (IOException ioe) {
            throw new DrigoException("Process failure on :" + src, ioe);
        }
    }

    /**
     * Return false if the file was modified
     * @param src
     * @param dst
     * @param results
     * @return
     * @throws IOException
     */
    public abstract boolean innerProcess(File src, File dst, DrigoResults results)
            throws IOException;

    public void logDeltaIfExists(File src, File dst)
            throws IOException {
        long srcL = src.length();
        long dstL = dst.length();
        long delta = Math.abs(dstL - srcL);
        int pct = (int) (100.0 * ((double) delta / (double) srcL));
        if (srcL != dstL) {
            log().info("Proc " + getClass().getSimpleName() + " mod file " + src.getName() + " from " + srcL + " to " + dstL + " (" + delta + " bytes " + pct + "%)");
        }
    }

    public Logger log() {
        return LoggerFactory.getLogger(getClass());
    }

}
