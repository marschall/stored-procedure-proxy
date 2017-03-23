package com.github.marschall.storedprocedureproxy.annotations;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration;

/**
 * Defines the name of an in parameter. Only used if the parameter registration
 * is either {@link ParameterRegistration#NAME_ONLY} or
 * {@link ParameterRegistration#NAME_AND_TYPE}.
 *
 * @see <a href="https://github.com/marschall/stored-procedure-proxy/wiki/Binding-Parameters">Binding Parameters</a>
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
