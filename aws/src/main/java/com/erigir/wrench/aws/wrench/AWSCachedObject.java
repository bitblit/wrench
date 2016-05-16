package com.erigir.wrench.aws.wrench;

/**
 * Objects implementing this interface allow easy caching of objects
 * in AWS locations like Dynamo or S3.
 * <p>
 * Basically, it offers "get" and "put" functionality, where the
 * "put" call is auto-backed by writing the state to the aws store
 * and the "get" call is auto-backed by reading from
 * store at startup.  Get will return null only if the
 * system has never been used before, and therefore there is no
 * item in the system (or if a null save is called - typically only
 * needed if the schema changes so drastically that you need to
 * nuke the store and start over)
 * <p>
 * Note that this always serves a COPY of the object, not the
 * cache itself.  Deep copying is done via ExtendedQuietObjectMapper's
 * cloning capability.
 * <p>
 * This class should be considered to have 2 basic functions - the "backing" of the
 * object (storing into AWS for permanence) and the "caching" of the object (storing
 * in memory for fast retrieval during program execution)
 * <p>
 * For situations where you don't need caching, wrench SimpleObjectStorage might
 * be a better solution
 * <p>
 * Created by cweiss1271 on 2/29/16.
 */
public interface AWSCachedObject<T> {

    /**
     * Called by the system on construction, but can also be called by other objects who want
     * to force a cache re-read
     */
    void forceCacheReload();

    void value(T value);

    T value();

}
