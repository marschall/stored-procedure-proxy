package com.github.marschall.storedprocedureproxy;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.github.marschall.storedprocedureproxy.spi.TypeNameResolver;

/**
 * Default implementation of {@link TypeNameResolver}.
 *
 * <p>The behavior is:</p>
 * <ol>
 *  <li>if a parameter name is present use its value</li>
 * </ol>
 *
 */
final class DefaultTypeNameResolver implements TypeNameResolver {

  static final TypeNameResolver INSTANCE = new DefaultTypeNameResolver();

  private final Map<Class<?>, String> typeMap;

  private DefaultTypeNameResolver() {
    this.typeMap = new HashMap<>();
    this.typeMap.put(String.class, "VARCHAR");

    // char is not mapped

    // limited precision integers
    this.typeMap.put(Integer.class, "INTEGER");
    this.typeMap.put(int.class, "INTEGER");
    this.typeMap.put(Long.class, "BIGINT");
    this.typeMap.put(long.class, "BIGINT");
    this.typeMap.put(Short.class, "SMALLINT");
    this.typeMap.put(short.class, "SMALLINT");
    this.typeMap.put(Byte.class, "TINYINT");
    this.typeMap.put(byte.class, "TINYINT");
    // arbitrary precision numbers
    // should be an alias for DECIMAL but Oracle treats DECIMAL as double
    this.typeMap.put(BigDecimal.class, "NUMERIC");
    this.typeMap.put(BigInteger.class, "NUMERIC");

    // floating points
    this.typeMap.put(Float.class, "REAL");
    this.typeMap.put(float.class, "REAL");
    this.typeMap.put(Double.class, "DOUBLE");
    this.typeMap.put(double.class, "DOUBLE");

    // java 8 date time
    this.typeMap.put(LocalDate.class, "DATE");
    this.typeMap.put(LocalTime.class, "TIME");
    this.typeMap.put(LocalDateTime.class, "TIMESTAMP");

    // old date time
    this.typeMap.put(java.sql.Date.class, "DATE");
    this.typeMap.put(java.sql.Time.class, "TIME");
    this.typeMap.put(java.sql.Timestamp.class, "TIMESTAMP");

    // boolean
    this.typeMap.put(Boolean.class, "BOOLEAN");
    this.typeMap.put(boolean.class, "BOOLEAN");
  }

  @Override
  public String resolveTypeName(Parameter parameter) {
    Class<?> parameterType = parameter.getType();
    Class<?> elementType;
    if (Collection.class.isAssignableFrom(parameterType)) {
      elementType = getCollectionTypeParameter(parameter);
    } else if (parameterType.isArray()) {
      elementType = parameterType.getComponentType();
    } else {
      throw new IllegalArgumentException("parameter " + parameter + " needs to be List or array");
    }

    String typeName = this.typeMap.get(elementType);
    if (typeName == null) {
      throw new IllegalArgumentException("SQL type for element type: " + elementType + " can not be determined");
    }
    return typeName;
  }

  private Class<?> getCollectionTypeParameter(Parameter parameter) {
    Type type = parameter.getParameterizedType();
    if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;
      Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
      if (actualTypeArguments.length != 1) {
        throw new IllegalArgumentException("type arguments of parameter " + parameter + " are missing");
      }
      Type actualTypeArgument = actualTypeArguments[0];
      if (!(actualTypeArgument instanceof Class)) {
        throw new IllegalArgumentException("type arguments of parameter" + parameter + " is not a class");
      }
      return ((Class<?>) actualTypeArgument);
    } else {
      throw new IllegalArgumentException("parameter " + parameter + " is missing type paramter for " + type);
    }
  }

}
