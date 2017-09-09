package com.erigir.wrench.drigo.processor;

import com.erigir.wrench.drigo.DrigoException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
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

/**
 * Ensure that a {@link File} can be parsed into JSON.
 */
public class JSONValidator implements Validator {

  @Override
  public void validate(File input) throws DrigoException {
    try {
      final JsonParser parser = new ObjectMapper().getFactory().createParser(input);

      while (parser.nextToken() != null) {
      }

    } catch (IOException e) {
      throw new DrigoException("JSON validation failed for: " + input, e);
    }
  }
}
