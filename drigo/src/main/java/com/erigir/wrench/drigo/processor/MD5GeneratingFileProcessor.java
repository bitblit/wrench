package com.erigir.wrench.drigo.processor;

import com.erigir.wrench.drigo.DrigoException;
import com.erigir.wrench.drigo.DrigoResults;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;

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
public class MD5GeneratingFileProcessor extends AbstractFileProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(MD5GeneratingFileProcessor.class);

    public boolean innerProcess(File src, File dst, DrigoResults results)
            throws DrigoException, IOException {
        byte[] md5 = DigestUtils.md5(new FileInputStream(src));
        String md5Hex = Hex.encodeHexString(md5);
        String md5Base64 = Base64.getEncoder().encodeToString(md5);
        LOG.trace("For file {}, md5hex: {} base64:{}", src, md5Hex, md5Base64);
        results.addMetadata(src, "md5-hex", md5Hex);
        results.addMetadata(src, "md5-base64", md5Base64);

        return false;
    }

}
