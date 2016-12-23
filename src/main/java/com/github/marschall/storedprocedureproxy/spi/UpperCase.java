package com.github.marschall.storedprocedureproxy.spi;

import java.util.Locale;

final class UpperCase implements NamingStrategy {

  static final NamingStrategy INSTANCE = new UpperCase();

  private UpperCase() {
    super();
  }

  @Override
  public String translateToDatabase(String javaName) {
    return javaName.toUpperCase(Locale.US);
  }

}
