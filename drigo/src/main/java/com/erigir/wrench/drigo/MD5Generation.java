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
 * Adds the MD5 of a file to its metadata
 */
public class MD5Generation {
  private static final Logger LOG = LoggerFactory.getLogger(MD5Generation.class);
  private String includeRegex;

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("MD5Generation[includeRegex=").append(includeRegex)
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

  public Pattern getIncludeRegexPattern() {
    return (includeRegex == null) ? null : Pattern.compile(includeRegex);
  }

}
