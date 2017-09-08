package com.erigir.wrench.drigo.processor;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.SourceFile;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;

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
public class TestRuntimeExecClosureCompiler {

  @Test
  public void testCompiler()
      throws Exception {
    File t1 = new File(getClass().getResource("/js/test1.js").getFile());
    //File t2 = new File(getClass().getResource("/js/test2.js").getFile());
    //List<File> files = Arrays.asList(t1, t2);

    //String input = IOUtils.toString(getClass().getResourceAsStream("/js/test1.js"));

    JavascriptCompilerFileProcessor cc = new JavascriptCompilerFileProcessor();

    String output = cc.compile(CompilationLevel.SIMPLE_OPTIMIZATIONS, Arrays.asList(SourceFile.fromFile(t1)));

    System.out.println("out: \n" + output);
  }


}
