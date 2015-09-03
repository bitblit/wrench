package com.erigir.wrench.drigo;

import com.erigir.wrench.drigo.processor.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
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

public class Drigo {
    private static final Logger LOG = LoggerFactory.getLogger(Drigo.class);

    public DrigoResults execute(DrigoConfiguration configuration) {
        DrigoResults rval = new DrigoResults();
        rval.setSourceConfiguration(configuration);
        try {

            /*
            if (objectMetadataSettings == null) {
                LOG.info("No upload configs specified, using default");
                objectMetadataSettings = new LinkedList<>();
            }
            */

            File src = configuration.getSrc();
            File dst = configuration.getDst();

            if (!src.exists()) {
                throw new DrigoException("File/folder doesn't exist: " + src);
            }

            if (dst == null) {
                LOG.info("No dst specified, creating temp directory");
                File sysTempDir = new File(System.getProperty("java.io.tmpdir"));
                dst = new File(sysTempDir, UUID.randomUUID().toString());
                configuration.setDst(dst);
                LOG.info("Using temp directory : {}", dst.getAbsoluteFile());
            }

            if (dst.exists() && configuration.isClearTargetBeforeProcessing()) {
                boolean wasDir = dst.isDirectory();
                LOG.info("{} exists and clearTarget is set - deleting", dst);
                dst.delete();
                if (wasDir) {
                    dst.mkdir();
                }
            }
            // This is designed to be easy to understand, not particularly efficient.  We'll
            // work on efficiency later

            // We start out by copying all of the files that arent excluded
            // Copy all files over
            FileProcessorUtils.copyFolder(src, dst, configuration.getExclusions());

            LOG.info("Checking rename mappings");
            if (configuration.getRenameMappings() != null) {
                for (RenameMapping r : configuration.getRenameMappings()) {
                    File input = new File(dst, r.getSrc());
                    if (input.exists()) {
                        File output = new File(dst, r.getDst());
                        LOG.info("Renaming {} to {}", input, output);
                        input.renameTo(output);
                    } else {
                        LOG.info("Rename Mapping {} doesnt exist, skipping", input);
                    }
                }

            }

            // Now, run the configured file validators
            LOG.info("Running validators");
            if (configuration.getValidation() != null) {

                for (ValidationSetting validator : configuration.getValidation()) {
                    ValidationProcessor proc = new ValidationProcessor(validator.getType());
                    applyProcessorToFileList(findMatchingFiles(dst, Pattern.compile(validator.getIncludeRegex())), proc, rval);
                }
            }

            LOG.info("Running simpleIncludes");
            if (configuration.getProcessIncludes() != null) {
                for (ProcessIncludes pi : configuration.getProcessIncludes()) {
                    DrigoSimpleIncludesProcessor dsi = new DrigoSimpleIncludesProcessor(pi.getPrefix(), pi.getSuffix());
                    for (File f : findMatchingFiles(dst, Pattern.compile(pi.getIncludeRegex()))) {
                        dsi.process(f, rval);
                    }
                }
            }

            LOG.info("Running replacement");
            if (configuration.getProcessReplace() != null) {
                ProcessReplace pr = configuration.getProcessReplace();
                DrigoReplaceProcessor drp = new DrigoReplaceProcessor(pr.getPrefix(),pr.getSuffix(),pr.getReplace());
                for (File f : findMatchingFiles(dst, Pattern.compile(pr.getIncludeRegex()))) {
                    drp.process(f, rval);
                }
            }

            // Now, do any batching
            LOG.info("Doing HTML resource batching");
            if (configuration.getHtmlResourceBatching() != null) {
                for (HtmlResourceBatching h : configuration.getHtmlResourceBatching()) {
                    List<File> matching = new LinkedList<>();
                    findMatchingFiles(dst, Pattern.compile(h.getIncludeRegex()), matching);

                    if (matching.size() > 0) {
                        File toOutput = new File(dst, h.getOutputFileName());
                        LOG.info("Creating output file : " + toOutput);
                        h.combine(matching, toOutput);

                        List<File> htmlToFilter = new LinkedList<>();
                        if (h.getReplaceInHtmlRegex() != null) {
                            ApplyHtmlBatchingFilterProcessor ap = new ApplyHtmlBatchingFilterProcessor(h);
                            applyProcessorToFileList(findMatchingFiles(dst, Pattern.compile(h.getReplaceInHtmlRegex())), ap, rval);
                        } else {
                            LOG.info("Not performing html replacement");
                        }
                    } else {
                        LOG.info("HTMLBatcher didn't find any files matching : " + h.getIncludeRegex() + ", skipping");
                    }
                }
            }

            // Now, apply Css compression if applicable
            LOG.info("Checking CSS compression");
            if (configuration.getCssCompilationIncludeRegex() != null) {
                YUICompileContentModelProcessor proc = new YUICompileContentModelProcessor();

                applyProcessorToFileList(findMatchingFiles(dst, configuration.getCssCompilationIncludeRegex()), proc, rval);
            }

            // Now, apply babel compilation if applicable
            LOG.info("Checking Babel Compilation");
            if (configuration.getBabelCompilationIncludeRegex() != null) {
                BabelCompilationProcessor proc = new BabelCompilationProcessor();

                applyProcessorToFileList(findMatchingFiles(dst, configuration.getBabelCompilationIncludeRegex()), proc, rval);
            }

            LOG.info("Checking JS compression");
            if (configuration.getJavascriptCompilation() != null) {
                JavascriptCompilerFileProcessor ipcc = new JavascriptCompilerFileProcessor();
                ipcc.setMode(configuration.getJavascriptCompilation().getMode());
                try {
                    applyProcessorToFileList(findMatchingFiles(dst, Pattern.compile(configuration.getJavascriptCompilation().getIncludeRegex())), ipcc, rval);
                } catch (Throwable t) {
                    LOG.error("Caught " + t);
                    throw t;
                }
            }

            LOG.info("Checking HTML compression");
            if (configuration.getHtmlCompression() != null) {
                HtmlCompressionProcessor hfp = new HtmlCompressionProcessor();
                applyProcessorToFileList(findMatchingFiles(dst, configuration.getHtmlCompression()), hfp, rval);
            }

            LOG.info("Checking GZIP compression");
            if (configuration.getFileCompressionIncludeRegex() != null) {
                GZipFileProcessor gzfp = new GZipFileProcessor();
                applyProcessorToFileList(findMatchingFiles(dst, configuration.getFileCompressionIncludeRegex()), gzfp, rval);
                LOG.info("GZIP compression saved " + GZipFileProcessor.totalSaved + " bytes in total");
            }

            LOG.info("Applying add metadata");
            if (configuration.getAddMetadata() != null) {
                for (AddMetadata a : configuration.getAddMetadata()) {
                    List<File> l = findMatchingFiles(dst, a.getIncludeRegexPattern());
                    for (File f : l) {
                        rval.addMetadata(f, a.getName(), a.getValue());
                    }

                }
            }

            LOG.info("Generation MD5 Signatures");
            if (configuration.getMd5GenerationIncludeRegex() != null) {
                MD5GeneratingFileProcessor md5fp = new MD5GeneratingFileProcessor();
                applyProcessorToFileList(findMatchingFiles(dst, configuration.getMd5GenerationIncludeRegex()), md5fp, rval);
            }


        } finally {
            LOG.info("Drigo: All processing finished.");
        }

        rval.setEndTime(System.currentTimeMillis());
        return rval;
    }

    public void applyProcessorToFileList(List<File> src, FileProcessor processor, DrigoResults results)
            throws DrigoException {
        assert (src != null && processor != null);

        for (File f : src) {
            LOG.info("Applying {} to {}", processor.getClass().getName(), src);
            processor.process(f, results);
            String current = results.fetchMetadata(f, DrigoResults.APPLIED_KEY);
            current = (current == null) ? processor.getClass().getSimpleName() : current + " " + processor.getClass().getSimpleName();
            results.addMetadata(f, DrigoResults.APPLIED_KEY, current);
        }
    }

    public List<File> findMatchingFiles(File src, Pattern pattern) {
        List<File> rval = new LinkedList<>();
        findMatchingFiles(src, pattern, rval);
        LOG.info("Found " + rval.size() + " files matching pattern " + pattern + " : " + rval);
        return rval;
    }

    public void findMatchingFiles(File src, Pattern pattern, List<File> matching) {
        assert (src != null && matching != null);
        if (src.isFile()) {
            if (pattern == null || pattern.matcher(src.getAbsolutePath()).matches()) {
                //LOG.info("Matching " + pattern + " to " + src);
                matching.add(src);
            }
        } else {
            for (String s : src.list()) {
                findMatchingFiles(new File(src, s), pattern, matching);
            }
        }
    }

}
