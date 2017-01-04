package com.github.marschall.storedprocedureproxy.examples;

import java.sql.CallableStatement;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;

public class JdbcTemplateTest extends AbstractExampleTest {

  private JdbcOperations jdbcOperations;

  @Before
  public void setUp() {
    this.jdbcOperations = new JdbcTemplate(this.getDataSource());
  }

  @Override
  protected int plus1inout(int arg) {
    List<SqlParameter> parameters = Arrays.asList(
            new SqlParameter("arg", Types.INTEGER),
            new SqlOutParameter("res", Types.INTEGER));

    Map<String, Object> results = this.jdbcOperations.call(con -> {
        CallableStatement statement = con.prepareCall("{call plus1inout(?, ?)}");
        statement.setInt(1, arg);
        statement.registerOutParameter(2, Types.INTEGER);
        return statement;
        }, parameters);
    return (Integer) results.get("res");
  }

}
