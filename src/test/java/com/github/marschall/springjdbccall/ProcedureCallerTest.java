package com.github.marschall.springjdbccall;

import static org.junit.Assert.assertArrayEquals;
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
    assertEquals("{call n()}", ProcedureCaller.buildQualifiedProcedureCallString(null, "n", 0));
    assertEquals("{call n(?)}", ProcedureCaller.buildQualifiedProcedureCallString(null, "n", 1));
    assertEquals("{call n(?,?)}", ProcedureCaller.buildQualifiedProcedureCallString(null, "n", 2));
  }

  @Test
  public void buildQualifiedFunctionCallString() {
    assertEquals("{ ? = call p.n()}", ProcedureCaller.buildQualifiedFunctionCallString("p", "n", 0));
    assertEquals("{ ? = call p.n(?)}", ProcedureCaller.buildQualifiedFunctionCallString("p", "n", 1));
    assertEquals("{ ? = call p.n(?,?)}", ProcedureCaller.buildQualifiedFunctionCallString("p", "n", 2));
  }

  @Test
  public void buildSimpleFunctionCallString() {
    assertEquals("{ ? = call n()}", ProcedureCaller.buildQualifiedFunctionCallString(null, "n", 0));
    assertEquals("{ ? = call n(?)}", ProcedureCaller.buildQualifiedFunctionCallString(null, "n", 1));
    assertEquals("{ ? = call n(?,?)}", ProcedureCaller.buildQualifiedFunctionCallString(null, "n", 2));
  }

  @Test
  public void buildInParameterIndicesNoOut() {
    assertArrayEquals(new int[] {}, ProcedureCaller.buildInParameterIndices(0));
    assertArrayEquals(new int[] {1}, ProcedureCaller.buildInParameterIndices(1));
    assertArrayEquals(new int[] {1, 2}, ProcedureCaller.buildInParameterIndices(2));
  }

  @Test
  public void buildInParameterIndicesWithOut() {
    assertArrayEquals(new int[] {}, ProcedureCaller.buildInParameterIndices(0, 1));
    assertArrayEquals(new int[] {2, 3, 4}, ProcedureCaller.buildInParameterIndices(3, 1));
    assertArrayEquals(new int[] {1, 3, 4}, ProcedureCaller.buildInParameterIndices(3, 2));
    assertArrayEquals(new int[] {1, 2, 4}, ProcedureCaller.buildInParameterIndices(3, 3));
  }


}
