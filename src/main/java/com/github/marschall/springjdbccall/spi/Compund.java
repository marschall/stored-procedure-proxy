package com.github.marschall.springjdbccall.spi;

import java.util.ArrayList;
import java.util.List;

final class Compund implements NamingStrategy {

  private final List<NamingStrategy> strategies;

  Compund(NamingStrategy first, NamingStrategy second) {
    this.strategies = new ArrayList<>(2);
    this.strategies.add(first);
    this.strategies.add(second);
  }

  @Override
  public String translateToDatabase(String javaName) {
    String databaseName = javaName;
    for (NamingStrategy strategy : this.strategies) {
      databaseName = strategy.translateToDatabase(databaseName);
    }
    return databaseName;
  }

  @Override
  public NamingStrategy thenUpperCase() {
    this.strategies.add(NamingStrategy.upperCase());
    return this;
  }

  @Override
  public NamingStrategy thenLowerCase() {
    this.strategies.add(NamingStrategy.lowerCase());
    return this;
  }

  @Override
  public NamingStrategy thenCapitalize() {
    this.strategies.add(NamingStrategy.capitalize());
    return this;
  }

  @Override
  public NamingStrategy thenSnakeCase() {
    this.strategies.add(NamingStrategy.snakeCase());
    return this;
  }

  @Override
  public NamingStrategy thenPrefix(String prefix) {
    this.strategies.add(NamingStrategy.prefix(prefix));
    return this;
  }

}
