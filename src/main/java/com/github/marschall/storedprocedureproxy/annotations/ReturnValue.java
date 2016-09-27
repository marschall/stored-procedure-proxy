package com.github.marschall.storedprocedureproxy.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration;
import com.github.marschall.storedprocedureproxy.spi.TypeMapper;

/**
 * Signals that the procedure uses a return value rather than an out
 * parameter or result set.
 *
 * <p>Also allows to provide additional information about the return value.</p>
 *
 * <p>You would use this for functions as opposed to procedures.</p>
 *
 * <p>Causes a call string to be generated in the form of
 * {@code "{ ? = call function_name()}"} instead of {@code "{call function_name(?)}"}.</p>
 *
 * @see OutParameter
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
