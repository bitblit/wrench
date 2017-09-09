package com.erigir.wrench.simpleincludes;

import com.erigir.wrench.ZipUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by cweiss on 8/4/15.
 */
public class SimpleIncludesFileSource implements SimpleIncludesSource {
  private static final Logger LOG = LoggerFactory.getLogger(SimpleIncludesFileSource.class);
  private File includeSourceParentDirectory;

  public SimpleIncludesFileSource() {
  }

  public SimpleIncludesFileSource(File includeSourceParentDirectory) {
    this.includeSourceParentDirectory = includeSourceParentDirectory;
  }

  @Override
  public String findContent(String name) {
    String rval = null;
    File contents = new File(includeSourceParentDirectory, name);
    if (contents.exists() && contents.isFile()) {
      try {
        FileInputStream fis = new FileInputStream(contents);
        byte[] data = ZipUtils.toByteArray(fis);
        fis.close();
        rval = new String(data);
      } catch (IOException ioe) {
        LOG.warn("Error occurred trying to read file, returning null", ioe);
        rval = null;
      }
    } else {
      LOG.warn("Requested file {}, but not found in {}", name, includeSourceParentDirectory);
    }

    return rval;
  }

  public void setIncludeSourceParentDirectory(File includeSourceParentDirectory) {
    this.includeSourceParentDirectory = includeSourceParentDirectory;
  }
}
