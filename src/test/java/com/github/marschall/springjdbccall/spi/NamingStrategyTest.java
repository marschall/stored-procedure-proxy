package com.github.marschall.springjdbccall.spi;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NamingStrategyTest {

  @Test
  public void spBlitz() {
    NamingStrategy strategy = NamingStrategy.capitalize()
      .thenPrefix("sp_");
    assertEquals("sp_Blitz", strategy.translateToDatabase("blitz"));
  }

  @Test
  public void snakeCase() {
    NamingStrategy strategy = NamingStrategy.snakeCase()
            .thenUpperCase();
    assertEquals("JAVA_NAME", strategy.translateToDatabase("javaName"));
  }

}
