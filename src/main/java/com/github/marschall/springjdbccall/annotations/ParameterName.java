package com.github.marschall.springjdbccall.annotations;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.marschall.springjdbccall.ProcedureCallerFactory.ParameterRegistration;

/**
 * Defines the name of an in parameter. Only used if the parameter registration
 * is either {@link ParameterRegistration#NAME_ONLY} or
 * {@link ParameterRegistration#NAME_AND_TYPE}.
 */
@Documented
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface ParameterName {

  /**
   * Defines the name of the in parameter.
   *
   * @return the name of the in parameter
   */
  String value();

}
