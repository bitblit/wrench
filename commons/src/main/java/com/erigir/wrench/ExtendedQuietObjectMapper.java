package com.erigir.wrench;


import com.fasterxml.jackson.core.type.TypeReference;

/**
 * A object mapper with some helper methods added.
 * <p>
 * I chose this structure because all of these methods require an object mapper,
 * so it makes sense (in some manner) to put them in here, but I wanted q
 * QuietObjectMapper to remain an exact replacement for ObjectMapper that I can
 * rev directly with the source version.  This way I get both.
 * <p>
 * <p>
 * Created by cweiss1271 on 2/29/16.
 */
public class ExtendedQuietObjectMapper extends QuietObjectMapper {

  /**
   * Safely (if not quickly) deep-duplicates the object provided by sending to JSON and back
   *
   * @param source Source object to duplicate
   * @param clazz  Class of object to return (for type erasure)
   * @param <T>    Class of object to return
   * @return copy of original object, or null if either of the original arguments were null
   */
  public <T> T safeDuplicate(T source, Class<T> clazz) {
    T rval = null;
    if (source != null && clazz != null) {
      // Not super efficient but safe
      String val = writeValueAsString(source);
      rval = readValue(val, clazz);
    }
    return rval;
  }

  /**
   * Safely (if not quickly) deep-duplicates the object provided by sending to JSON and back
   *
   * @param source        Source object to duplicate
   * @param typeReference TypeReference of object to return (for type erasure)
   * @param <T>           Class of object to return
   * @return copy of original object, or null if either of the original arguments were null
   */
  public <T> T safeDuplicate(T source, TypeReference<T> typeReference) {
    T rval = null;
    if (source != null && typeReference != null) {
      // Not super efficient but safe
      String val = writeValueAsString(source);
      rval = readValue(val, typeReference);
    }
    return rval;
  }


}
