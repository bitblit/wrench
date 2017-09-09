package com.erigir.wrench.slf4j;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * RingBuffer to store logging output from MemoryAppender for consumption inside the program.
 * <p>
 * Created by cweiss on 2/14/16.
 */
public class LoggingRingBuffer {
  public static final LoggingRingBuffer INST = new LoggingRingBuffer();
  private static int RING_SIZE = 40;
  private List<String> buffer = new LinkedList<>();

  private LoggingRingBuffer() {
    super();
  }

  public static void setRingSize(int newSize) {
    LoggingRingBuffer.RING_SIZE = newSize;

    // In case its a down adjustment
    INST.fitRingToSize();
  }

  public void addItem(Object item) {
    buffer.add(0, String.valueOf(item));
    fitRingToSize();
  }

  public List<String> getData() {
    return new ArrayList<>(buffer);

  }

  private void fitRingToSize() {
    // TODO: this could be a lot more efficent
    while (buffer.size() > RING_SIZE) {
      buffer.remove(buffer.size() - 1);
    }
  }

}
