package com.erigir.wrench.drigo.processor;

import com.erigir.wrench.drigo.DrigoException;
import com.erigir.wrench.drigo.DrigoResults;
import com.erigir.wrench.drigo.JavascriptCompilation;
import com.google.common.collect.Lists;
import com.google.javascript.jscomp.*;
import com.google.javascript.jscomp.Compiler;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
 * <p>
 * NOTE : Borrowed heavily from http://blog.bolinfest.com/2009/11/calling-closure-compiler-from-java.html
 **/
public class JavascriptCompilerFileProcessor extends AbstractFileProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(JavascriptCompilerFileProcessor.class);
    private JavascriptCompilation.JSCompilationMode mode;
    private List<SourceFile> cacheDefaultExterns;

    public boolean innerProcess(File src, File dst, DrigoResults results) throws DrigoException, IOException {
        CompilationLevel level = fromMode();
        LOG.trace("ClosureCompile at level {} on :{}", level, src);
        String out = compile(level, getDefaultExterns(), Arrays.asList(SourceFile.fromFile(src)));
        IOUtils.write(out, new FileOutputStream(dst));
        return true;
    }

    public final String compile(CompilationLevel level, String code)
            throws IOException {
        return compile(level, Arrays.asList(SourceFile.fromCode("input.js", code)));
    }

    public final String compile(CompilationLevel level, List<SourceFile> input)
            throws IOException {
        return compile(level, getDefaultExterns(), input);
    }

    /**
     * Compiles javascript using the closure compiler (or possibly others in the future)
     *
     * @param level   CompilationLevel to compile at
     * @param externs List&lt;SourceFile&gt; to treat as externs for the compilation
     * @param input   JavaScript source code to compile.
     * @return String containing the compiled version of the code.
     * @throws IOException on read/write failure
     */
    public final String compile(CompilationLevel level, List<SourceFile> externs, List<SourceFile> input)
            throws IOException {
        Compiler compiler = new Compiler();

        CompilerOptions options = new CompilerOptions();
        // level is used here, but additional options could be set, too.
        level.setOptionsForCompilationLevel(
                options);

        Result results = compiler.compile(externs, input, options);

        if (results.success) {
            // The compiler is responsible for generating the compiled code; it is not
            // accessible via the Result.
            return compiler.toSource();
        } else {
            // Failed - just return the uncompressed stuff
            StringBuilder sb = new StringBuilder();
            for (SourceFile s : input) {
                sb.append(s.getCode());
                sb.append("\n");
            }
            return sb.toString();
        }
    }

    private CompilationLevel fromMode() {
        CompilationLevel rval = null;
        switch (mode) {
            case CLOSURE_WHITESPACE:
                rval = CompilationLevel.WHITESPACE_ONLY;
                break;
            case CLOSURE_BASIC:
                rval = CompilationLevel.SIMPLE_OPTIMIZATIONS;
                break;
            case CLOSURE_ADVANCED:
                rval = CompilationLevel.ADVANCED_OPTIMIZATIONS;
                break;
            default:
                rval = CompilationLevel.WHITESPACE_ONLY;
                break;
        }
        return rval;
    }

    private synchronized List<SourceFile> getDefaultExterns() throws IOException {
        if (cacheDefaultExterns == null) {
            cacheDefaultExterns = buildDefaultExterns();
        }
        return cacheDefaultExterns;
    }

    /**
     * These come from Google as part of the package
     *
     * @return a mutable list
     * @throws IOException
     */
    private List<SourceFile> buildDefaultExterns() throws IOException {

        File tempFile = File.createTempFile("def-ext", ".zip");
        IOUtils.copy(Compiler.class.getResourceAsStream(
                "/externs.zip"), new FileOutputStream(tempFile));

        List<SourceFile> externs = Lists.newLinkedList();


        ZipFile zipFile = new ZipFile(tempFile);

        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            InputStream stream = zipFile.getInputStream(entry);
            externs.add(SourceFile.fromInputStream(entry.getName(), stream, Charset.forName("UTF-8")));
        }

        return externs;
    }

    public void setMode(JavascriptCompilation.JSCompilationMode mode) {
        this.mode = mode;
    }
}
