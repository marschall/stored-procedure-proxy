package com.github.marschall.springjdbccall.spi;

final class Capitalize implements NamingStrategy {

  static final NamingStrategy INSTANCE = new Capitalize();

  @Override
  public String translateToDatabase(String javaName) {
    int length = javaName.length();
    if (length == 0) {
      return javaName;
    }
    StringBuilder builder = new StringBuilder(length);
    char first = javaName.charAt(0);
    if (first >= 'a' && first <= 'z') {
      builder.append((char) (first + ('A' - 'a')));
    } else {
      builder.append(first);
    }
    if (length > 1) {
      builder.append(javaName, 1, length);
    }
    return builder.toString();
  }

}
