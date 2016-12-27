package com.github.marschall.storedprocedureproxy;

import java.lang.reflect.Parameter;

import com.github.marschall.storedprocedureproxy.spi.TypeNameResolver;

final class DelegatingTypeNameResolver implements TypeNameResolver {

  private final TypeNameResolver delegate;

  DelegatingTypeNameResolver(TypeNameResolver delegate) {
    this.delegate = delegate;
  }

  @Override
  public String resolveTypeName(Parameter parameter) {
    String typeName = AnnotationBasedTypeNameResolver.INSTANCE.resolveTypeName(parameter);
    if (typeName != null) {
      return typeName;
    } else {
      return this.delegate.resolveTypeName(parameter);
    }
  }

}
