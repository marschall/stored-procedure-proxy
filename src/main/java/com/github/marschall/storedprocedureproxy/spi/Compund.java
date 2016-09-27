package com.github.marschall.storedprocedureproxy.spi;

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
  public NamingStrategy then(NamingStrategy next) {
    this.strategies.add(next);
    return this;
  }

}
