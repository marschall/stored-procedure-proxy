package com.github.marschall.springjdbccall;

import static com.github.marschall.springjdbccall.ProcedureCallerFactory.ProcedureCaller.*;
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
    assertEquals("{call n()}", ProcedureCaller.buildSimpleProcudureCallString("n", 0));
    assertEquals("{call n(?)}", ProcedureCaller.buildSimpleProcudureCallString("n", 1));
    assertEquals("{call n(?,?)}", ProcedureCaller.buildSimpleProcudureCallString("n", 2));
  }

  @Test
  public void buildQualifiedFunctionCallString() {
    assertEquals("{ ? = call p.n()}", ProcedureCaller.buildQualifiedFunctionCallString("p", "n", 0));
    assertEquals("{ ? = call p.n(?)}", ProcedureCaller.buildQualifiedFunctionCallString("p", "n", 1));
    assertEquals("{ ? = call p.n(?,?)}", ProcedureCaller.buildQualifiedFunctionCallString("p", "n", 2));
  }

  @Test
  public void buildSimpleFunctionCallString() {
    assertEquals("{ ? = call n()}", ProcedureCaller.buildSimpleFunctionCallString("n", 0));
    assertEquals("{ ? = call n(?)}", ProcedureCaller.buildSimpleFunctionCallString("n", 1));
    assertEquals("{ ? = call n(?,?)}", ProcedureCaller.buildSimpleFunctionCallString("n", 2));
  }

}
