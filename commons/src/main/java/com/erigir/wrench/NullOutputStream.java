package com.erigir.wrench;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Equivalent to /dev/null on unix
 * <p>
 * Created by cweiss1271 on 5/25/16.
 */
public class NullOutputStream extends OutputStream {
  public static NullOutputStream NULL_OUTPUT_STREAM = new NullOutputStream();

  // No reason to ever have more than one of these
  private NullOutputStream() {
    super();
  }

  @Override
  public void write(int b) throws IOException {
    // Do nothing
  }
}
