package com.github.marschall.storedprocedureproxy.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Defines the namespace of a stored procedure.
 *
 * <p>This should be used for Oracle
 * <a href="https://docs.oracle.com/database/121/LNPLS/packages.htm#LNPLS009">PL/SQL Packages</a>
 * or IBM <a href="https://www.ibm.com/support/knowledgecenter/SSEPGG_11.1.0/com.ibm.db2.luw.apdv.sqlpl.doc/doc/c0053740.html">DB2 Modules</a>.</p>
 *
 * @see <a href="https://github.com/marschall/stored-procedure-proxy/wiki/Oracle-Packages">Oracle Packages</a>
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface Namespace {


  /**
   * Defines the namespace of a stored procedure.
   *
   * @return the namespace of a stored procedure
   */
  String value() default "";

}
