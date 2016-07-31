package com.github.marschall.springjdbccall;

public class H2ProcedureDefinition {

  public static String stringFunction(String input) {
    return "pre" + input + "post";
  }

}
