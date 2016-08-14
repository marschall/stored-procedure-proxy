package com.github.marschall.springjdbccall.spi;

@FunctionalInterface
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

  default NamingStrategy then(NamingStrategy next) {
    return new Compund(this, next);
  }

  default NamingStrategy thenUpperCase() {
    return then(upperCase());
  }

  default NamingStrategy thenLowerCase() {
    return then(lowerCase());
  }

  default NamingStrategy thenCapitalize() {
    return then(capitalize());
  }

  default NamingStrategy thenSnakeCase() {
    return then(snakeCase());
  }

  default NamingStrategy thenPrefix(String prefix) {
    return then(prefix(prefix));
  }

}
