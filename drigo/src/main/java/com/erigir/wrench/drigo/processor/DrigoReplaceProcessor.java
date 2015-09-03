package com.erigir.wrench.drigo.processor;

import com.erigir.wrench.drigo.DrigoResults;
import com.erigir.wrench.simpleincludes.SimpleIncludesFileSource;
import com.erigir.wrench.simpleincludes.SimpleIncludesPatternMapSource;
import com.erigir.wrench.simpleincludes.SimpleIncludesProcessor;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
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

/**
 * Uses the Wrench simple includes processor to string based replacing
 */
public class DrigoReplaceProcessor extends AbstractFileProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(DrigoReplaceProcessor.class);
    private String prefix;
    private String suffix;
    private LinkedHashMap<Pattern, String> replace;

    public DrigoReplaceProcessor(String prefix, String suffix,LinkedHashMap<Pattern, String> replace) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.replace = replace;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public boolean innerProcess(File src, File dst, DrigoResults results)
            throws IOException {
        SimpleIncludesPatternMapSource sifs = new SimpleIncludesPatternMapSource(replace);
        SimpleIncludesProcessor sip = new SimpleIncludesProcessor(sifs, prefix, suffix);

        String input = IOUtils.toString(new FileInputStream(src));
        String output = sip.processIncludes(input);
        FileWriter fw = new FileWriter(dst);
        fw.write(output);
        fw.close();

        return true;
    }

}
