package com.erigir.wrench;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * A simple singleton that allows one to "fake" the system clock.
 * <p>
 * Once "started", it advances in normal time just like the system clock does,
 * but it can be set to start from any arbitrary time.  Useful for systems that
 * work on a clock schedule but need to be tested against static data.  Only
 * downside is that the consuming system must call SystemClock.nowTime instead of
 * System.currentTimeMillis everywhere in the code.
 * </p>
 * <p>
 * There is also an "error offset", set by queries against a time server like NTP, which allows
 * for fixing issues with the system clock.
 * </p>
 * <p>
 * The "total" offset is these 2 added together, and NOW = System.currentTimeMillis() + total offset
 * </p>
 *
 * Created by cweiss on 2/10/16.
 */
public class SystemClock {
  private static long TESTING_OFFSET = 0L;
  private static long ERROR_OFFSET = 0L;

  private SystemClock() {
    super();
  }

  public static void forceStartTime(Date timestamp) {
    TESTING_OFFSET = System.currentTimeMillis() - timestamp.getTime();
  }

  public static void setErrorOffset(long errorOffset) {
    ERROR_OFFSET = errorOffset;
  }

  public static long testingOffset() {
    return TESTING_OFFSET;
  }

  public static long errorOffset() {
    return ERROR_OFFSET;
  }

  public static long totalOffset() {
    return TESTING_OFFSET + ERROR_OFFSET;
  }

  public static Long nowErrorOnlyApplied() {
    return System.currentTimeMillis() + ERROR_OFFSET;
  }

  public static Long now() {
    return System.currentTimeMillis() + totalOffset();
  }

  public static Date nowDate() {
    return new Date(now());
  }

  public static ZonedDateTime zonedDateTime(ZoneId id) {
    return ZonedDateTime.ofInstant(nowDate().toInstant(), id);
  }

}

