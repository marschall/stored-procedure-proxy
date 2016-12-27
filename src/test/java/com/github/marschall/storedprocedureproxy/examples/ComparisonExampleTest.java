package com.github.marschall.storedprocedureproxy.examples;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory;
import com.github.marschall.storedprocedureproxy.procedures.ComparisonExample;

public class ComparisonExampleTest extends AbstractExampleTest {

  private ComparisonExample procedures;

  @Before
  public void setUp() {
    this.procedures = ProcedureCallerFactory.build(ComparisonExample.class, this.getDataSource());
  }

  @Test
  public void procedure() {
    assertEquals(2, this.procedures.plus1inout(1));
  }

}
