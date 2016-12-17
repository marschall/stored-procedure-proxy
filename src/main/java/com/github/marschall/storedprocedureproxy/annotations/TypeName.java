package com.github.marschall.storedprocedureproxy.annotations;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.sql.Connection;

/**
 * Defines the name of a type. Useful for array types.
 *
 * @see Connection#createArrayOf(String, Object[])
 */
@Documented
@Retention(RUNTIME)
@Target({PARAMETER, TYPE_USE})
public @interface TypeName {

  /**
   * Defines the name of a type.
   *
   * @return the name of a type
   */
  String value();

}
