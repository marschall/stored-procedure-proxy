package com.github.marschall.springjdbccall.spi;

@FunctionalInterface
public interface TypeMapper {

  int mapToSqlType(Class<?> javaType);

}
