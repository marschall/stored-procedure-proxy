package com.github.marschall.storedprocedureproxy;

import static com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration.INDEX_AND_TYPE;
import static com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration.INDEX_ONLY;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
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
import org.springframework.transaction.annotation.Transactional;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration;
import com.github.marschall.storedprocedureproxy.configuration.PostgresConfiguration;
import com.github.marschall.storedprocedureproxy.configuration.TestConfiguration;
import com.github.marschall.storedprocedureproxy.procedures.PostgresProcedures;

@RunWith(Parameterized.class)
@Transactional
@ContextConfiguration(classes = {PostgresConfiguration.class, TestConfiguration.class})
public class PostgresTest {

  @ClassRule
  public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

  @Rule
  public final SpringMethodRule springMethodRule = new SpringMethodRule();

  @Autowired
  private DataSource dataSource;

  private PostgresProcedures procedures;

  private ParameterRegistration parameterRegistration;

  public PostgresTest(ParameterRegistration parameterRegistration) {
    this.parameterRegistration = parameterRegistration;
  }

  @Before
  public void setUp() {
    this.procedures = ProcedureCallerFactory.of(PostgresProcedures.class, this.dataSource)
            .withParameterRegistration(this.parameterRegistration)
            .build();

    EncodedResource resource = new EncodedResource(new ClassPathResource("postgres_procedures.sql"), UTF_8);
    DatabasePopulator populator = new PostgresDatabasePopulator("LANGUAGE plpgsql;", resource);
    DatabasePopulatorUtils.execute(populator, this.dataSource);
  }

  @Parameters
  public static Collection<Object[]> parameters() {
    return Arrays.asList(
            new Object[] {INDEX_ONLY},
            new Object[] {INDEX_AND_TYPE}
    );
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

  @Test
  public void simpleRefCursor() {
    List<String> refCursor = this.procedures.simpleRefCursor();
    assertEquals(Arrays.asList("hello", "postgres"), refCursor);
  }

  @Test
  public void simpleRefCursorOut() {
    List<String> refCursor = this.procedures.simpleRefCursorOut();
    assertEquals(Arrays.asList("hello", "postgres"), refCursor);
  }

  @Test
  public void mappedRefCursor() {
    List<String> refCursor = this.procedures.mappedRefCursor((rs, i) -> "1-" + rs.getString(1));
    assertEquals(Arrays.asList("1-hello", "1-postgres"), refCursor);
  }

  @Test
  public void sampleArrayArgumentList() {
    String result = this.procedures.sampleArrayArgumentList(Arrays.asList(1, 2, 3));
    assertEquals("1, 2, 3", result);
  }

  @Test
  public void sampleArrayArgumentArray() {
    String result = this.procedures.sampleArrayArgumentArray(new Integer[] {1, 2, 3});
    assertEquals("1, 2, 3", result);
  }

  @Test
  public void sampleArrayArgumentPrimitiveArray() {
    String result = this.procedures.sampleArrayArgumentPrimitiveArray(new int[] {1, 2, 3});
    assertEquals("1, 2, 3", result);
  }

  @Test
  public void nativeArrayCall() throws SQLException {
    try (Connection connection = this.dataSource.getConnection();
         CallableStatement call = connection.prepareCall("{? = call sample_array_argument(?)}")) {
      call.registerOutParameter(1, Types.VARCHAR);
      Array array = connection.createArrayOf("int", new Object[] {1, 2, 3});
      call.setObject(2, array);

      try {
        assertFalse(call.execute());
        assertEquals("1, 2, 3", call.getString(1));
      } finally {
        array.free();
      }
    }
  }

}
