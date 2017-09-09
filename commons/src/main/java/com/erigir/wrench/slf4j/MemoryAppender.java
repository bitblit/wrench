package com.erigir.wrench.slf4j;

import ch.qos.logback.core.AppenderBase;

/**
 * Implementation of appender that writes everything logged into LoggingRingBuffer.
 * <p>
 * LoggingRingBuffer is a static class so that its easy to get a handle to it from both client code
 * and MemoryAppender which is typically instantiated by Slf4J itself.
 * <p>
 * This combo is typically used when you want to look at the logging info from the system itself (for
 * example, to display it on the screen)
 * <p>
 * Config for slf4j should look like :
 * <p>
 * &lt;appender name="MEMORY_DEBUG" class="com.erigir.wrench.slf4j.MemoryAppender"&gt;
 * &lt;encoder&gt;
 * &lt;pattern&gt;%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n&lt;/pattern&gt;
 * &lt;/encoder&gt;
 * &lt;/appender&gt;
 * <p>
 * and be sure to add
 * &lt;appender-ref ref="MEMORY_DEBUG"/&gt;
 * to your root appender
 * <p>
 * Created by cweiss on 2/14/16.
 */
public class MemoryAppender extends AppenderBase {
  @Override
  protected void append(Object o) {
    LoggingRingBuffer.INST.addItem(o);
  }
}
