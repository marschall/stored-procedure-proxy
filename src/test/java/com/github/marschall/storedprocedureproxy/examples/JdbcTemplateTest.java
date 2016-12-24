package com.github.marschall.storedprocedureproxy.examples;

import static org.junit.Assert.assertEquals;

import java.sql.CallableStatement;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.transaction.annotation.Transactional;

import com.github.marschall.storedprocedureproxy.configuration.HsqlConfiguration;
import com.github.marschall.storedprocedureproxy.configuration.TestConfiguration;

@Transactional
@ContextConfiguration(classes = {HsqlConfiguration.class, TestConfiguration.class})
public class JdbcTemplateTest {

  @ClassRule
  public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

  @Rule
  public final SpringMethodRule springMethodRule = new SpringMethodRule();

  @Autowired
  private DataSource dataSource;

  private JdbcOperations jdbcOperations;

  @Before
  public void setUp() {
    this.jdbcOperations = new JdbcTemplate(this.dataSource);
  }

  @Test
  public void call() {
    assertEquals(2, plus1inout(1));
  }

  private int plus1inout(int argument) {
    List<SqlParameter> parameters = Arrays.asList(
            new SqlParameter("arg", Types.INTEGER),
            new SqlOutParameter("res", Types.INTEGER));

    Map<String, Object> results = this.jdbcOperations.call(con -> {
        CallableStatement statement = con.prepareCall("{call plus1inout(?, ?)}");
        statement.setInt(1, argument);
        statement.registerOutParameter(2, Types.INTEGER);
        return statement;
        }, parameters);
    return (Integer) results.get("res");
  }

}