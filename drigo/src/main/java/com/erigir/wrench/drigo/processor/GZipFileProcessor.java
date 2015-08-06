package com.erigir.wrench.drigo.processor;

import com.erigir.wrench.drigo.DrigoException;
import com.erigir.wrench.drigo.DrigoResults;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.zip.GZIPOutputStream;

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
public class GZipFileProcessor extends AbstractFileProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(ApplyHtmlBatchingFilterProcessor.class);

    public static int totalSaved = 0;

    public boolean innerProcess(File src, File dst, DrigoResults results)
            throws DrigoException, IOException {

        InputStream is = new FileInputStream(src);
        OutputStream os = new GZIPOutputStream(new FileOutputStream(dst));

        IOUtils.copy(is, os);

        IOUtils.closeQuietly(is);
        IOUtils.closeQuietly(os);

        long delta = dst.length() - src.length();
        totalSaved += delta;

        results.addMetadata(src, "content-encoding", "gzip");

        return true;
    }

}
