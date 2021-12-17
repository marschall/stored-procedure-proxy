package com.github.marschall.storedprocedureproxy.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration;
import com.github.marschall.storedprocedureproxy.spi.TypeMapper;

/**
 * Signals that the procedure uses an out parameter rather than a
 * return value or result set.
 *
 * <p>Applied to a method means that the return value should be retrieved using an
 * <a href="https://en.wikipedia.org/wiki/Parameter_(computer_programming)#Output_parameters">out parameter</a>.
 * In addition allows to provide additional information about the out parameter.</p>
 *
 * <p>Causes a call string to be generated in the form of
 * {@code {@code "{call function_name(?)}" instead of "{ ? = call function_name()}"}}
 * where one of the function arguments is an out parameter.</p>
 *
 * @see ReturnValue
 * @see InOutParameter
 * @see <a href="https://github.com/marschall/stored-procedure-proxy/wiki/Result-Extraction#out-parameter">Out Parameter Result Extraction</a>
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface OutParameter {

  /**
   * Defines the SQL type of the out parameter. If nothing is specified
   * the default from {@link TypeMapper} is used.
   *
   * @return the out parameter SQL type, can be a vendor type
   * @see java.sql.Types
   */
  int type() default Integer.MIN_VALUE;

  /**
   * Defines the index of the out parameter. If not specified the out
   * parameter is assumed to be the last parameter.
   *
   * <p>If the out parameter isn't the last parameter you have to
   * provide the index of the out parameter.</p>
   *
   * @return the 1 based index of the out parameter
   */
  int index() default -1;

  /**
   * Defines the name of the out parameter. Only used if the parameter
   * registration is either {@link ParameterRegistration#NAME_ONLY} or
   * {@link ParameterRegistration#NAME_AND_TYPE}.
   *
   * @return the name of the out parameter
   */
  String name() default "";

  /**
   * If the out parameter is a a user defined type like a named array
   * types then this method denotes the type name.
   *
   * @return the fully-qualified name of an SQL structured type
   * @see java.sql.CallableStatement#registerOutParameter(int, int, String)
   */
  String typeName() default "";

}
