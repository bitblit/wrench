package com.erigir.wrench.drigo.processor;

import com.erigir.wrench.drigo.DrigoException;
import org.junit.Test;

import java.io.File;

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

public class TestValidators {

  @Test
  public void testValidJSON()
      throws Exception {
    File goodJSON = new File(getClass().getResource("/validate/good.json").getFile());

    JSONValidator jsonValidator = new JSONValidator();
    jsonValidator.validate(goodJSON);
  }

  @Test(expected = DrigoException.class)
  public void testInValidJSON()
      throws Exception {
    File badJSON = new File(getClass().getResource("/validate/bad.json").getFile());

    JSONValidator jsonValidator = new JSONValidator();
    jsonValidator.validate(badJSON);
  }

  @Test
  public void testValidXML()
      throws Exception {
    File goodXML = new File(getClass().getResource("/validate/good.xml").getFile());

    XMLValidator xmlValidator = new XMLValidator();
    xmlValidator.validate(goodXML);
  }

  @Test(expected = DrigoException.class)
  public void testInValidXML()
      throws Exception {
    File badXML = new File(getClass().getResource("/validate/bad.xml").getFile());

    XMLValidator xmlValidator = new XMLValidator();
    xmlValidator.validate(badXML);
  }
}
