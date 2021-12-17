package com.github.marschall.storedprocedureproxy.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Signals that the procedure uses an inout parameter rather than
 * an out parameter, a return value or result set.
 *
 * <p>Applied to a method means that the return value should be retrieved using an
 * <a href="https://en.wikipedia.org/wiki/Parameter_(computer_programming)#Output_parameters">inout parameter</a>.</p>
 *
 * <p>Unlike {@link OutParameter} or {@link ReturnValue} most additional
 * information in taken from the method parameter</p>
 *
 * <p>Causes a call string to be generated in the form of
 * {@code {@code "{call function_name(?)}" instead of "{ ? = call function_name()}"}}
 * where one of the function arguments is an out parameter.</p>
 *
 * @see ReturnValue
 * @see OutParameter
 * @see <a href="https://github.com/marschall/stored-procedure-proxy/wiki/Result-Extraction#inout-parameter">InOut Parameter Result Extraction</a>
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface InOutParameter {

  /**
   * Defines the index of the inout parameter. If not specified the
   * inout parameter is assumed to be the last parameter.
   *
   * <p>If the out parameter isn't the last parameter you have to
   * provide the index of the out parameter.</p>
   *
   * @return the 1 based index of the out parameter
   */
  int index() default -1;

}
