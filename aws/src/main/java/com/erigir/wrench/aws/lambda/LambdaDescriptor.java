package com.erigir.wrench.aws.lambda;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * An annotation that can be applied to a function so that it can
 * later be scanned and uploaded to lambda mechanically
 * Created by cweiss1271 on 12/1/15.
 */
@Target( { METHOD })
@Retention(RUNTIME)
@Documented
public @interface LambdaDescriptor {
    String functionName();
    String description();
    String runtime() default "Java8";
    int timeout() default 60;
    int memorySize() default 128;
    String lambdaRoleArn();
}
