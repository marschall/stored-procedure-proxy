package com.github.marschall.storedprocedureproxy.spi;

import java.util.Locale;

final class LowerCase implements NamingStrategy {

  static final NamingStrategy INSTANCE = new LowerCase();

  @Override
  public String translateToDatabase(String javaName) {
    return javaName.toLowerCase(Locale.US);
  }

}
