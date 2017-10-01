package com.github.marschall.storedprocedureproxy;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ProcedureCaller;

public class ProcedureCallerTest {

  @Test
  public void buildQualifiedProcedureCallString() {
    assertEquals("{call p.n()}", ProcedureCaller.buildQualifiedProcedureCallString(null, "p", "n", 0));
    assertEquals("{call p.n(?)}", ProcedureCaller.buildQualifiedProcedureCallString(null, "p", "n", 1));
    assertEquals("{call p.n(?,?)}", ProcedureCaller.buildQualifiedProcedureCallString(null, "p", "n", 2));
  }

  @Test
  public void buildSimpleProcudureCallString() {
    assertEquals("{call n()}", ProcedureCaller.buildQualifiedProcedureCallString(null, null, "n", 0));
    assertEquals("{call n(?)}", ProcedureCaller.buildQualifiedProcedureCallString(null, null, "n", 1));
    assertEquals("{call n(?,?)}", ProcedureCaller.buildQualifiedProcedureCallString(null, null, "n", 2));
  }

  @Test
  public void buildQualifiedFunctionCallString() {
    assertEquals("{ ? = call p.n()}", ProcedureCaller.buildQualifiedFunctionCallString(null, "p", "n", 0));
    assertEquals("{ ? = call p.n(?)}", ProcedureCaller.buildQualifiedFunctionCallString(null, "p", "n", 1));
    assertEquals("{ ? = call p.n(?,?)}", ProcedureCaller.buildQualifiedFunctionCallString(null, "p", "n", 2));
  }

  @Test
  public void buildSimpleFunctionCallString() {
    assertEquals("{ ? = call n()}", ProcedureCaller.buildQualifiedFunctionCallString(null, null, "n", 0));
    assertEquals("{ ? = call n(?)}", ProcedureCaller.buildQualifiedFunctionCallString(null, null, "n", 1));
    assertEquals("{ ? = call n(?,?)}", ProcedureCaller.buildQualifiedFunctionCallString(null, null, "n", 2));
  }

  @Test
  public void buildInParameterIndicesNoOut() {
    assertArrayEquals(new byte[] {}, ProcedureCaller.buildInParameterIndices(0, new Class[0]));
    assertArrayEquals(new byte[] {1}, ProcedureCaller.buildInParameterIndices(1, new Class[] {String.class}));
    assertArrayEquals(new byte[] {1, 2}, ProcedureCaller.buildInParameterIndices(2, new Class[] {String.class, String.class}));
  }

  @Test
  public void buildInParameterIndicesWithOut() {
    assertArrayEquals(new byte[] {}, ProcedureCaller.buildInParameterIndices(0, 1, new Class[0]));
    assertArrayEquals(new byte[] {2, 3, 4}, ProcedureCaller.buildInParameterIndices(3, 1, new Class[] {String.class, String.class, String.class}));
    assertArrayEquals(new byte[] {1, 3, 4}, ProcedureCaller.buildInParameterIndices(3, 2, new Class[] {String.class, String.class, String.class}));
    assertArrayEquals(new byte[] {1, 2, 4}, ProcedureCaller.buildInParameterIndices(3, 3, new Class[] {String.class, String.class, String.class}));
  }


}
