package com.github.marschall.springjdbccall;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

@ContextConfiguration(classes = {PostgresConfiguration.class, TestConfiguration.class})
public class PostgresTest {

  @ClassRule
  public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

  @Rule
  public final SpringMethodRule springMethodRule = new SpringMethodRule();

  @Autowired
  private DataSource dataSource;

  private PostgresProcedures procedures;

  @Before
  public void setUp() {
    this.procedures = ProcedureCallerFactory.of(PostgresProcedures.class, this.dataSource)
            .build();

    EncodedResource resource = new EncodedResource(new ClassPathResource("postgres_procedures.sql"), UTF_8);
    DatabasePopulator populator = new PostgresDatabasePopulator("LANGUAGE plpgsql;", resource);
    DatabasePopulatorUtils.execute(populator, this.dataSource);
  }

  @Test
  public void browserVersion() {
    assertEquals("Servo/0.0.1", this.procedures.browserVersion("Servo", "0.0.1"));
  }

  @Test
  public void callSalesTaxOld() throws SQLException {
    try (Connection connection = this.dataSource.getConnection();
            CallableStatement statement = connection.prepareCall("{ ? = call sales_tax(?)}")) {
      statement.registerOutParameter(1, Types.REAL);
      statement.setFloat(2, 100.0f);

      boolean hasResult = statement.execute();
      assertFalse(hasResult);
      Object result = statement.getObject(1);
      assertEquals(0.01d, 6.0f, (Float) result);
    }
  }

  @Test
  public void salesTax() {
    assertEquals(0.01f, 6.0f, this.procedures.salesTax(100.0f));
  }

  @Test
  public void callPropertyTaxOld() throws SQLException {
    try (Connection connection = this.dataSource.getConnection();
            CallableStatement statement = connection.prepareCall("{call property_tax(?, ?)}")) {
      statement.setFloat(1, 100.0f);
      statement.registerOutParameter(2, Types.REAL);

      boolean hasResult = statement.execute();
      assertFalse(hasResult);
      Object result = statement.getObject(2);
      assertEquals(0.01d, 6.0f, (Float) result);
    }
  }

  @Test
  public void propertyTax() {
    assertEquals(0.01f, 6.0f, this.procedures.propertyTax(100.0f));
  }

  @Test
  public void raiseCheckedException() {
    try {
      this.procedures.raiseCheckedException();
      fail("should raise exception");
    } catch (SQLException e) {
      assertTrue(e instanceof PSQLException);
      assertEquals("22000", e.getSQLState());
    }
  }

  @Test
  public void raiseUncheckedException() {
    try {
      this.procedures.raiseUncheckedException();
      fail("should raise exception");
    } catch (DataAccessException e) {
      Throwable cause = e.getCause();
      assertTrue(cause instanceof PSQLException);
      assertEquals("22000", ((SQLException) cause).getSQLState());
    }
  }

}
