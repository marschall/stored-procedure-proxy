package com.github.marschall.storedprocedureproxy.spi;

import java.lang.reflect.Parameter;
import java.sql.Connection;

/**
 * Resolves the SQL name of a type.
 *
 * @see Connection#createArrayOf(String, Object[])
 */
@FunctionalInterface
public interface TypeNameResolver {

  /**
   * Resolve the SQL name of a type.
   *
   * @param parameter the method parameter who's type
   * @return the SQL name of the type
   */
  String getTypeName(Parameter parameter);

}
