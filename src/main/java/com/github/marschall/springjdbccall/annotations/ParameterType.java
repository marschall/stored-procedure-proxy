package com.github.marschall.springjdbccall.annotations;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.marschall.springjdbccall.spi.TypeMapper;

/**
 * Defines the SQL type of an IN parameter.
 */
@Documented
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface ParameterType {

  /**
   * Defines the SQL type of an IN parameter. If nothing is specified the default
   * from {@link TypeMapper} is used.
   *
   * @return the parameter SQL type, can be a vendor type
   * @see java.sql.Types
   */
  int value();

}
