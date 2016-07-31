package com.github.marschall.springjdbccall.spi;

public class Capitalize implements NamingStrategy {

  static final NamingStrategy INSTANCE = new Capitalize();

  @Override
  public String translateToDatabase(String javaName) {
    // TODO Auto-generated method stub
    return null;
  }

}
