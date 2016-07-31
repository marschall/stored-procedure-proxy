package com.github.marschall.springjdbccall.spi;

public interface NamingStrategy {

  public static NamingStrategy IDENTITY = (s) -> s;

  String translateToDatabase(String javaName);

}
