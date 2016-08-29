package com.github.marschall.springjdbccall.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.marschall.springjdbccall.ProcedureCallerFactory.ParameterRegistration;
import com.github.marschall.springjdbccall.spi.TypeMapper;

/**
 * Applied to a method means that the return value should be retrieved using an
 * <a href="https://en.wikipedia.org/wiki/Parameter_(computer_programming)#Output_parameters">out parameter</a>.
 * In addition allows to provide additional information about the out parameter.
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface OutParameter {


  /**
   * Defines the index of the out parameter. If not specified the out parameter
   * is assumed to be the last parameter.
   *
   * <p>If the out parameter isn't the last parameter you have to
   * provide the index of the out parameter.<p>
   *
   * @return the 1 based index of the out parameter
   */
  // TODO default index 1 or last
  // TODO 1 or 0 based
  int index() default -1;

  /**
   * Defines the name of the out parameter. Only used if the parameter registration
   * is either {@link ParameterRegistration#NAME_ONLY} or
   * {@link ParameterRegistration#NAME_AND_TYPE}.
   *
   * @return the name of the out parameter
   */
  String name() default "";

  /**
   * Defines the SQL type of the out parameter. If nothing is specified the default
   * from {@link TypeMapper} is used.
   *
   * @return the out parameter SQL type, can be a vendor type
   * @see java.sql.Types
   */
  int type() default Integer.MIN_VALUE;

}
