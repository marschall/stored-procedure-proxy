package com.github.marschall.storedprocedureproxy.spi;

final class Prefix implements NamingStrategy {

  private final String prefix;

  Prefix(String prefix) {
    this.prefix = prefix;
  }

  @Override
  public String translateToDatabase(String javaName) {
    return this.prefix + javaName;
  }

}
