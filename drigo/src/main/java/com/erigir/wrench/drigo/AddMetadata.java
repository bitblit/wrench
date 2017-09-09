package com.erigir.wrench.drigo;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * Adds an arbitrary piece of metadata to the matching files
 */
public class AddMetadata {
  private static final Logger LOG = LoggerFactory.getLogger(AddMetadata.class);
  private String includeRegex;
  private String name;
  private String value;

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("AddMetadata[includeRegex=").append(includeRegex)
        .append(", name=").append(name)
        .append(", value=").append(value)
        .append("]");
    return sb.toString();
  }

  public String getIncludeRegex() {
    return includeRegex;
  }

  public void setIncludeRegex(String includeRegex) {
    if (includeRegex == null) {
      throw new IllegalArgumentException("Cannot set includeRegex to null");
    }
    this.includeRegex = includeRegex;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public Pattern getIncludeRegexPattern() {
    return (includeRegex == null) ? null : Pattern.compile(includeRegex);
  }

}
