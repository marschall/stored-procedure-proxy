package com.github.marschall.storedprocedureproxy.spi;

final class WithoutFirst implements NamingStrategy {

  private final int skipped;

  WithoutFirst(int skipped) {
    this.skipped = skipped;
  }

  @Override
  public String translateToDatabase(String javaName) {
    return javaName.substring(skipped);
  }

}
