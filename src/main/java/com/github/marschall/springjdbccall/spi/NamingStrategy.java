package com.github.marschall.springjdbccall.spi;

public interface NamingStrategy {

  public static NamingStrategy IDENTITY = (s) -> s;

  String translateToDatabase(String javaName);

  public static NamingStrategy upperCase() {
    return UpperCase.INSTANCE;
  }

  public static NamingStrategy lowerCase() {
    return LowerCase.INSTANCE;
  }

  public static NamingStrategy capitalize() {
    return Capitalize.INSTANCE;
  }

  public static NamingStrategy snakeCase() {
    return SnakeCase.INSTANCE;
  }

  public static NamingStrategy prefix(String prefix) {
    return new Prefix(prefix);
  }

  default NamingStrategy thenUpperCase() {
    return new Compund(this, upperCase());
  }

  default NamingStrategy thenLowerCase() {
    return new Compund(this, lowerCase());
  }

  default NamingStrategy thenCapitalize() {
    return new Compund(this, capitalize());
  }

  default NamingStrategy thenSnakeCase() {
    return new Compund(this, snakeCase());
  }

  default NamingStrategy thenPrefix(String prefix) {
    return new Compund(this, prefix(prefix));
  }

}
