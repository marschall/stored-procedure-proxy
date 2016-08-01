package com.github.marschall.springjdbccall.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Signals that the procedure uses a return value rather than an out
 * parameter.
 *
 * <p>You would use this for example for Oracle functions.</p>
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface ReturnValue {

}
