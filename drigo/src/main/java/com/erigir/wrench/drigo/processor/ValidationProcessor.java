package com.erigir.wrench.drigo.processor;

import com.erigir.wrench.drigo.DrigoException;
import com.erigir.wrench.drigo.DrigoResults;
import com.erigir.wrench.drigo.ValidationSetting;
import org.apache.commons.io.IOUtils;

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

public class ValidationProcessor extends AbstractFileProcessor {

    private final Validator validator;

    public ValidationProcessor(ValidationSetting.ValidationType type) {

        switch (type) {
            case JSON:
                validator = new JSONValidator();
                break;
            case XML:
                validator = new XMLValidator();
                break;
            default:
                throw new IllegalArgumentException("Cannot set validator type to: " + type);
        }
    }

    @Override
    public boolean innerProcess(File src, File dst, DrigoResults results) throws DrigoException, IOException {
        validator.validate(src);

        String input = IOUtils.toString(new FileInputStream(src));
        IOUtils.write(input, new FileOutputStream(dst));
        return true;
    }
}
