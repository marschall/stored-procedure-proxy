package com.github.marschall.storedprocedureproxy;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

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

  @Override
  public String getTypeName(Parameter parameter) {
    if (parameter.isNamePresent()) {
      return parameter.getName();
    }

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
      return ((Class<?>) actualTypeArgument).getSimpleName().toUpperCase();
    } else {
      throw new IllegalArgumentException("parameter " + parameter + " is missing type paramter for " + type);
    }
  }

}
