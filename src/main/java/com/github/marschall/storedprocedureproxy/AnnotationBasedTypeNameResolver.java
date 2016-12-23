package com.github.marschall.storedprocedureproxy;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Parameter;

import com.github.marschall.storedprocedureproxy.annotations.TypeName;
import com.github.marschall.storedprocedureproxy.spi.TypeNameResolver;

final class AnnotationBasedTypeNameResolver implements TypeNameResolver {

  static final TypeNameResolver INSTANCE = new AnnotationBasedTypeNameResolver();

  private AnnotationBasedTypeNameResolver() {
    super();
  }

  @Override
  public String resolveTypeName(Parameter parameter) {
    // REVIEW maybe move to dedicated reflection class
    if (parameter.isAnnotationPresent(TypeName.class)) {
      return parameter.getAnnotation(TypeName.class).value();
    } else {
      AnnotatedType annotatedType = parameter.getAnnotatedType();
      if (annotatedType instanceof AnnotatedParameterizedType) {
        AnnotatedParameterizedType annotatedParameterizedType = (AnnotatedParameterizedType) annotatedType;
        AnnotatedType[] annotatedActualTypeArguments = annotatedParameterizedType.getAnnotatedActualTypeArguments();
        if (annotatedActualTypeArguments != null && annotatedActualTypeArguments.length > 0) {
          AnnotatedType stringParameterType = annotatedActualTypeArguments[0];
          if (stringParameterType.isAnnotationPresent(TypeName.class)) {
            return stringParameterType.getAnnotation(TypeName.class).value();
          }
        }
      }
      return null;
    }
  }

}
