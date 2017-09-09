package com.erigir.wrench.drigo.processor;

import com.erigir.wrench.drigo.DrigoException;
import com.erigir.wrench.drigo.DrigoResults;
import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
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
public class HtmlCompressionProcessor extends AbstractFileProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(ApplyHtmlBatchingFilterProcessor.class);
  public static int totalSaved = 0;
  private HtmlCompressor htmlCompressor = new HtmlCompressor();

  public boolean innerProcess(File src, File dst, DrigoResults results)
      throws DrigoException, IOException {

    String input = IOUtils.toString(new FileInputStream(src));
    String output = htmlCompressor.compress(input);
    FileOutputStream fos = new FileOutputStream(dst);
    IOUtils.write(output, fos);
    IOUtils.closeQuietly(fos);

    long delta = dst.length() - src.length();
    totalSaved += delta;

    return true;
  }

}
