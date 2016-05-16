package com.erigir.wrench;

import java.util.Date;

/**
 * A simple singleton that allows one to "fake" the system clock.
 * Once "started", it advances in normal time just like the system clock does,
 * but it can be set to start from any arbitrary time.  Useful for systems that
 * work on a clock schedule but need to be tested against static data.  Only
 * downside is that the consuming system must call SystemClock.nowTime instead of
 * System.currentTimeMillis everywhere in the code.
 * <p>
 * Created by cweiss on 2/10/16.
 */
public class SystemClock {
    //private static final SystemClock INSTANCE = new SystemClock();
    // This is the actual clock time when the class started
    // Needed so that we can track time progress
    private static long FIXED_START_TIME;
    private static long START_TIME;

    private SystemClock() {
        super();
        FIXED_START_TIME = System.currentTimeMillis();
        START_TIME = FIXED_START_TIME;
    }

    public static void forceStartTime(Date timestamp) {
        FIXED_START_TIME = System.currentTimeMillis();
        START_TIME = timestamp.getTime();
    }

    public static Long now() {
        return START_TIME + delta();
    }

    public static Date nowDate() {
        return new Date(now());
    }

    public static Long delta() {
        return System.currentTimeMillis() - FIXED_START_TIME;
    }

    public static void forceStartTimeToSystem() {
        forceStartTime(new Date());
    }

    public static Long startTime() {
        return START_TIME;
    }

}
