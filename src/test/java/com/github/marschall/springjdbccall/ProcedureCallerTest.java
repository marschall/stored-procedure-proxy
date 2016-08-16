package com.github.marschall.springjdbccall;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.marschall.springjdbccall.ProcedureCallerFactory.ProcedureCaller;

public class ProcedureCallerTest {

  @Test
  public void buildQualifiedProcedureCallString() {
    assertEquals("{call p.n()}", ProcedureCaller.buildQualifiedProcedureCallString("p", "n", 0));
    assertEquals("{call p.n(?)}", ProcedureCaller.buildQualifiedProcedureCallString("p", "n", 1));
    assertEquals("{call p.n(?,?)}", ProcedureCaller.buildQualifiedProcedureCallString("p", "n", 2));
  }

  @Test
  public void buildSimpleProcudureCallString() {
    assertEquals("{call n()}", ProcedureCaller.buildQualifiedProcedureCallString("n", null, 0));
    assertEquals("{call n(?)}", ProcedureCaller.buildQualifiedProcedureCallString("n", null, 1));
    assertEquals("{call n(?,?)}", ProcedureCaller.buildQualifiedProcedureCallString("n", null, 2));
  }

  @Test
  public void buildQualifiedFunctionCallString() {
    assertEquals("{ ? = call p.n()}", ProcedureCaller.buildQualifiedFunctionCallString("p", "n", 0));
    assertEquals("{ ? = call p.n(?)}", ProcedureCaller.buildQualifiedFunctionCallString("p", "n", 1));
    assertEquals("{ ? = call p.n(?,?)}", ProcedureCaller.buildQualifiedFunctionCallString("p", "n", 2));
  }

  @Test
  public void buildSimpleFunctionCallString() {
    assertEquals("{ ? = call n()}", ProcedureCaller.buildQualifiedFunctionCallString("n", null, 0));
    assertEquals("{ ? = call n(?)}", ProcedureCaller.buildQualifiedFunctionCallString("n", null, 1));
    assertEquals("{ ? = call n(?,?)}", ProcedureCaller.buildQualifiedFunctionCallString("n", null, 2));
  }

}
