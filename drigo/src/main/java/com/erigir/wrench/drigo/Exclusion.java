package com.erigir.wrench.drigo;

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

import java.io.File;
import java.util.List;

/**
 * Files matching exclusion are neither copied nor processed
 */
public class Exclusion {

    private String includeRegex;

    public String getIncludeRegex() {
        return includeRegex;
    }

    public void setIncludeRegex(String includeRegex) {
        if (includeRegex == null) {
            throw new IllegalArgumentException("Cannot set includeRegex to null");
        }
        this.includeRegex = includeRegex;
    }

    public static boolean excluded(List<Exclusion> exclusionList, File f) {
        boolean rval = false;
        if (exclusionList != null && exclusionList.size() > 0) {
            for (int i = 0; i < exclusionList.size() && !rval; i++) {
                rval = FileProcessorUtils.matches(f, exclusionList.get(i).getIncludeRegex());
            }
        }
        return rval;
    }
}
