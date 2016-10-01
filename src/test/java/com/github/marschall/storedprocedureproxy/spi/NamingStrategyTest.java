package com.github.marschall.storedprocedureproxy.spi;

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

  @Test
  public void lowerCase() {
    NamingStrategy strategy = NamingStrategy.snakeCase()
            .thenLowerCase();
    assertEquals("java_name", strategy.translateToDatabase("javaName"));
  }

  @Test
  public void withoutFirst() {
    NamingStrategy strategy = NamingStrategy.withoutFirst(2);
    assertEquals("javaName", strategy.translateToDatabase("x_javaName"));
  }

  @Test
  public void compound() {
    NamingStrategy strategy = NamingStrategy.withoutFirst(2)
            .thenSnakeCase()
            .thenLowerCase();
    assertEquals("java_name", strategy.translateToDatabase("x_javaName"));
  }

}
