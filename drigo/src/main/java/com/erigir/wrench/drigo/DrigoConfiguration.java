package com.erigir.wrench.drigo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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

/**
 * A configuration of what Drigo should do to the given set of files
 */
public class DrigoConfiguration {
  private static final Logger LOG = LoggerFactory.getLogger(DrigoConfiguration.class);

  /**
   * The source file/folder to process
   */
  private File src;
  /**
   * The destincation file/folder to write
   */
  private File dst;

  /**
   * If set, the target directory will be deleted and recreated before processing begins
   */
  private boolean clearTargetBeforeProcessing = false;
  private List<AddMetadata> addMetadata;
  private List<HtmlResourceBatching> htmlResourceBatching;
  private Pattern fileCompressionIncludeRegex;
  private Pattern cssCompilationIncludeRegex;
  private Pattern babelCompilationIncludeRegex;
  private Pattern md5GenerationIncludeRegex;
  private JavascriptCompilation javascriptCompilation;
  private List<ValidationSetting> validation;
  private List<RenameMapping> renameMappings;
  private List<Exclusion> exclusions;
  private List<ProcessIncludes> processIncludes;
  private ProcessReplace processReplace;
  private Pattern htmlCompression;

  public List<ProcessIncludes> getProcessIncludes() {
    return processIncludes;
  }

  public void setProcessIncludes(List<ProcessIncludes> processIncludes) {
    this.processIncludes = processIncludes;
  }

  public List<AddMetadata> getAddMetadata() {
    return addMetadata;
  }

  public void setAddMetadata(List<AddMetadata> addMetadata) {
    this.addMetadata = addMetadata;
  }

  public List<HtmlResourceBatching> getHtmlResourceBatching() {
    return htmlResourceBatching;
  }

  public void setHtmlResourceBatching(List<HtmlResourceBatching> htmlResourceBatching) {
    this.htmlResourceBatching = htmlResourceBatching;
  }

  public Pattern getFileCompressionIncludeRegex() {
    return fileCompressionIncludeRegex;
  }

  public void setFileCompressionIncludeRegex(Pattern fileCompressionIncludeRegex) {
    this.fileCompressionIncludeRegex = fileCompressionIncludeRegex;
  }

  public Pattern getCssCompilationIncludeRegex() {
    return cssCompilationIncludeRegex;
  }

  public void setCssCompilationIncludeRegex(Pattern cssCompilationIncludeRegex) {
    this.cssCompilationIncludeRegex = cssCompilationIncludeRegex;
  }

  public JavascriptCompilation getJavascriptCompilation() {
    return javascriptCompilation;
  }

  public void setJavascriptCompilation(JavascriptCompilation javascriptCompilation) {
    this.javascriptCompilation = javascriptCompilation;
  }

  public List<ValidationSetting> getValidation() {
    return validation;
  }

  public void setValidation(List<ValidationSetting> validation) {
    this.validation = validation;
  }

  public List<RenameMapping> getRenameMappings() {
    return renameMappings;
  }

  public void setRenameMappings(List<RenameMapping> renameMappings) {
    this.renameMappings = renameMappings;
  }

  public List<Exclusion> getExclusions() {
    return exclusions;
  }

  public void setExclusions(List<Exclusion> exclusions) {
    this.exclusions = exclusions;
  }

  public File getSrc() {
    return src;
  }

  public void setSrc(File src) {
    this.src = src;
  }

  public File getDst() {
    return dst;
  }

  public void setDst(File dst) {
    this.dst = dst;
  }

  public boolean isClearTargetBeforeProcessing() {
    return clearTargetBeforeProcessing;
  }

  public void setClearTargetBeforeProcessing(boolean clearTargetBeforeProcessing) {
    this.clearTargetBeforeProcessing = clearTargetBeforeProcessing;
  }

  public Pattern getBabelCompilationIncludeRegex() {
    return babelCompilationIncludeRegex;
  }

  public void setBabelCompilationIncludeRegex(Pattern babelCompilationIncludeRegex) {
    this.babelCompilationIncludeRegex = babelCompilationIncludeRegex;
  }

  public Pattern getMd5GenerationIncludeRegex() {
    return md5GenerationIncludeRegex;
  }

  public void setMd5GenerationIncludeRegex(Pattern md5GenerationIncludeRegex) {
    this.md5GenerationIncludeRegex = md5GenerationIncludeRegex;
  }

  public ProcessReplace getProcessReplace() {
    return processReplace;
  }

  public void setProcessReplace(ProcessReplace processReplace) {
    this.processReplace = processReplace;
  }

  public Pattern getHtmlCompression() {
    return htmlCompression;
  }

  public void setHtmlCompression(Pattern htmlCompression) {
    this.htmlCompression = htmlCompression;
  }
}
