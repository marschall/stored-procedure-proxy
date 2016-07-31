package com.github.marschall.springjdbccall.spi;

final class SnakeCase implements NamingStrategy {

  static final NamingStrategy INSTANCE = new SnakeCase();

  @Override
  public String translateToDatabase(String javaName) {
    StringBuilder builder = new StringBuilder();
    boolean wasUpperCase = false;
    for (int i = 0; i < javaName.length(); i++) {
      char c = javaName.charAt(i);
      if (i != 0) {
        boolean isUpperCase = isUpperCase(c);
        if (isUpperCase && !wasUpperCase) {
          builder.append('_');
        }
        wasUpperCase = isUpperCase;
      }
      builder.append(c);
    }
    return builder.toString();
  }

  private static boolean isUpperCase(char c) {
    return c >= 'A' && c <= 'Z';
  }

}
