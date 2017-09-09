package com.erigir.wrench.ape.http;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * If a method in a ape exception has this annotation, then that methods rval is used as the 'data' field in the resp
 * Created by chrweiss on 7/1/14.
 */
@Target( {TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Documented
public @interface ApeException {
  int httpStatusCode() default 400;

  int detailCode();

  String message();

  String developerMessage();

  String detailObjectPropertyName() default "";
}
