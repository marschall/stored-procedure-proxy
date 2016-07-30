package com.github.marschall.springjdbccall.spi;

public interface TypeResolver {

  int translateToDatabase(Class<?> javaType);

}
