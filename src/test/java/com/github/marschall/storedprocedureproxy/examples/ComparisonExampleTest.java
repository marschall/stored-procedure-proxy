package com.github.marschall.storedprocedureproxy.examples;

import org.junit.Before;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory;
import com.github.marschall.storedprocedureproxy.procedures.ComparisonExample;

public class ComparisonExampleTest extends AbstractExampleTest {

  private ComparisonExample procedures;

  @Before
  public void setUp() {
    this.procedures = ProcedureCallerFactory.build(ComparisonExample.class, this.getDataSource());
  }

  @Override
  protected int plus1inout(int arg) {
    return this.procedures.plus1inout(arg);
  }

}
