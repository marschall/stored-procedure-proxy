package com.github.marschall.springjdbccall.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.marschall.springjdbccall.ProcedureCallerFactory.ParameterRegistration;
import com.github.marschall.springjdbccall.spi.TypeMapper;

/**
 * Signals that the procedure uses a return value rather than an out
 * parameter.
 *
 * <p>You would use this for functions versus procedures.</p>
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface ReturnValue {

  /**
   * Defines the SQL type of the return value. If nothing is specified the default
   * from {@link TypeMapper} is used.
   *
   * @return the return value SQL type, can be a vendor type
   * @see java.sql.Types
   */
  int type() default Integer.MIN_VALUE;

  /**
   * Defines the name of the return value. Only used if the parameter registration
   * is either {@link ParameterRegistration#NAME_ONLY} or
   * {@link ParameterRegistration#NAME_AND_TYPE}.
   *
   * @return the name of the return value
   */
  String name() default "";

}
