package com.github.marschall.storedprocedureproxy.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Defines the name of a database schema.
 *
 * <p>If the schema name is not static you can use something like this:</p>
 * <pre><code>ProcedureCallerFactory.of(MyProcedures.class, dataSource)
 *  .withSchemaNamingStrategy(ignored -> computeSchemaName())
 *  .build();</code></pre>
 *
 * <p>For PL/SQL packages or DB2 modules {@link Namespace} should be
 * used.</p>
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface Schema {


  /**
   * Defines the name of the database schema.
   *
   * @return the name of the database schema
   */
  String value() default "";

}
