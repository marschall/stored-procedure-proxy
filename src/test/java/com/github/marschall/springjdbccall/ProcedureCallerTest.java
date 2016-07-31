package com.github.marschall.springjdbccall;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.marschall.springjdbccall.ProcedureCallerFactory.ProcedureCaller;

public class ProcedureCallerTest {

  @Test
  public void buildQualifiedCallString() {
    assertEquals("{call p.n()}", ProcedureCaller.buildQualifiedCallString("p", "n", 0));
    assertEquals("{call p.n(?)}", ProcedureCaller.buildQualifiedCallString("p", "n", 1));
    assertEquals("{call p.n(?,?)}", ProcedureCaller.buildQualifiedCallString("p", "n", 2));
  }

  @Test
  public void buildSimpleCallString() {
    assertEquals("{call n()}", ProcedureCaller.buildSimpleCallString("n", 0));
    assertEquals("{call n(?)}", ProcedureCaller.buildSimpleCallString("n", 1));
    assertEquals("{call n(?,?)}", ProcedureCaller.buildSimpleCallString("n", 2));
  }

}
