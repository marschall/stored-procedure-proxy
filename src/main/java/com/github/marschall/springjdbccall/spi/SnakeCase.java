package com.github.marschall.springjdbccall.spi;

final class SnakeCase implements NamingStrategy {

  static final NamingStrategy INSTANCE = new SnakeCase();

  @Override
  public String translateToDatabase(String javaName) {
    // TODO Auto-generated method stub
    return null;
  }

}