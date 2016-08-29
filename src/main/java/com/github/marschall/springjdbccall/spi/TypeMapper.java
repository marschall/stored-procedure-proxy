package com.github.marschall.springjdbccall.spi;

import com.github.marschall.springjdbccall.annotations.OutParameter;
import com.github.marschall.springjdbccall.annotations.ParameterType;
import com.github.marschall.springjdbccall.annotations.ReturnValue;

/**
 * Maps a Java type to a SQL type. This is especially useful if you
 * want to use vendor types.
 *
 * <p>The mapping defined by an instance of this class will be applied
 * globally to all int and out parameter of all methods in an interface.
 * If you want to customize only a single parameter use
 * {@link ParameterType}, {@link OutParameter#type()} or
 * {@link ReturnValue#type()} instead.</p>
 */
@FunctionalInterface
public interface TypeMapper {

  /**
   * Maps a Java type to a SQL type.
   *
   * @see java.sql.Types
   * @param javaType
   *          the java type, may be a primitive type like {@code int.class},
   *          never {@code null}, never {@code void.class}
   * @return the SQL type, may be a vendor type
   */
  int mapToSqlType(Class<?> javaType);

}
