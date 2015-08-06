package com.erigir.wrench.drigo.processor;

import com.erigir.wrench.drigo.DrigoException;
import com.erigir.wrench.drigo.DrigoResults;
import com.erigir.wrench.drigo.HtmlResourceBatching;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
public class ApplyHtmlBatchingFilterProcessor extends AbstractFileProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(ApplyHtmlBatchingFilterProcessor.class);

    private HtmlResourceBatching batching;

    public ApplyHtmlBatchingFilterProcessor(HtmlResourceBatching batching) {
        this.batching = batching;
    }

    public boolean innerProcess(File src, File dst, DrigoResults results)
            throws IOException {
        String contents = IOUtils.toString(new FileInputStream(src));

        String startTag = "<!--" + batching.getFlagName() + "-->";
        String endTag = "<!--END:" + batching.getFlagName() + "-->";

        LOG.info("Searching " + src.getName() + " for " + startTag + " to " + endTag);
        int startIdx = contents.indexOf(startTag);

        int count = 0;
        while (startIdx != -1) {
            int endIdx = contents.indexOf(endTag);
            if (endIdx == -1) {
                throw new DrigoException("Couldn't find end tag : " + endTag);
            }
            count++;
            contents = contents.substring(0, startIdx) + batching.getWrappedReplaceText() + contents.substring(endIdx + (endTag.length()));
            startIdx = contents.indexOf(startTag);
        }

        FileOutputStream os = new FileOutputStream(dst);
        IOUtils.write(contents, os);
        IOUtils.closeQuietly(os);

        LOG.info("Found " + count + " instances");

        return true;
    }

}
