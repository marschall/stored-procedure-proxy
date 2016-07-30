package com.github.marschall.springjdbccall.spi;

public interface NamingStrategy {

  String translateToDatabase(String javaName);

}
