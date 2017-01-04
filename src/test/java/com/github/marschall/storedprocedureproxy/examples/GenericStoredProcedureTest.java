package com.github.marschall.storedprocedureproxy.examples;


import java.sql.Types;
import java.util.Map;

import org.junit.Before;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.GenericStoredProcedure;
import org.springframework.jdbc.object.StoredProcedure;

public class GenericStoredProcedureTest extends AbstractExampleTest {

  private StoredProcedure storedProcedure;

  @Before
  public void setUp() {
    storedProcedure = new GenericStoredProcedure();
    storedProcedure.setDataSource(this.getDataSource());
    storedProcedure.setSql("plus1inout");
    storedProcedure.setFunction(false);

    storedProcedure.declareParameter(new SqlParameter("arg", Types.INTEGER));
    storedProcedure.declareParameter(new SqlOutParameter("res", Types.INTEGER));
    storedProcedure.compile();
  }

  @Override
  protected int plus1inout(int arg) {
    Map<String, Object> results = this.storedProcedure.execute(arg);
    return (Integer) results.get("res");
  }

}
