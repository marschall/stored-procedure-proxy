package com.github.marschall.storedprocedureproxy.examples;

import static org.junit.Assert.assertEquals;

import java.sql.Types;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.StoredProcedure;

public class StoredProcedureTest extends AbstractExampleTest {

  private Plus1inout storedProcedure;

  @Before
  public void setUp() {
    this.storedProcedure = new Plus1inout(new JdbcTemplate(this.getDataSource()));
  }

  @Test
  public void call() {
    assertEquals(2, this.storedProcedure.plus1inout(1));
  }

  static final class Plus1inout extends StoredProcedure {

    Plus1inout(JdbcTemplate jdbcTemplate) {
      super(jdbcTemplate, "plus1inout");
      setFunction(false);
      // names have to be supplied even if they are not used
      // constructors are heavily overloaded with String and int
      declareParameter(new SqlParameter("arg", Types.INTEGER));
      declareParameter(new SqlOutParameter("res", Types.INTEGER));
      compile();
    }

    int plus1inout(int arg) {
      Map<String, Object> results = execute(arg);
      return (Integer) results.get("res");
    }

  }

}
