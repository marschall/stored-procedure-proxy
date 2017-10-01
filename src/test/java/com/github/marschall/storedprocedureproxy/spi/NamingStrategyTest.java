package com.github.marschall.storedprocedureproxy.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

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
  public void thenCapitalize() {
    NamingStrategy strategy = NamingStrategy.prefix("sp_")
            .thenCapitalize();
    assertEquals("Sp_blitz", strategy.translateToDatabase("blitz"));

  }

  @Test
  public void capitalize() {
    NamingStrategy strategy = NamingStrategy.capitalize();
    assertEquals("Java", strategy.translateToDatabase("java"));
    assertEquals("", strategy.translateToDatabase(""));
    assertEquals("1", strategy.translateToDatabase("1"));
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

  @Test
  public void moreCompound() {
    NamingStrategy strategy = NamingStrategy.snakeCase()
            .thenUpperCase()
            .thenWithoutFirst(2);
    assertEquals("JAVA_NAME", strategy.translateToDatabase("xJavaName"));
  }

}
