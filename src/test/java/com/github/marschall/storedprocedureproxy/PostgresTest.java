package com.github.marschall.storedprocedureproxy;

import static com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration.INDEX_AND_TYPE;
import static com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration.INDEX_ONLY;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.postgresql.util.PSQLException;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.transaction.annotation.Transactional;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration;
import com.github.marschall.storedprocedureproxy.configuration.PostgresConfiguration;
import com.github.marschall.storedprocedureproxy.procedures.PostgresProcedures;

@RunWith(Parameterized.class)
@Transactional
@ContextConfiguration(classes = PostgresConfiguration.class)
@Sql(scripts = "classpath:postgres_procedures.sql", config = @SqlConfig(separator = "@"))
public class PostgresTest extends AbstractDataSourceTest {

  private PostgresProcedures procedures;

  private ParameterRegistration parameterRegistration;

  public PostgresTest(ParameterRegistration parameterRegistration) {
    this.parameterRegistration = parameterRegistration;
  }

  @Before
  public void setUp() {
    this.procedures = ProcedureCallerFactory.of(PostgresProcedures.class, this.getDataSource())
            .withParameterRegistration(this.parameterRegistration)
            .build();
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
  public void salesTax() {
    assertEquals(0.01f, 6.0f, this.procedures.salesTax(100.0f));
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
  public void mappedRefCursorNumbered() {
    List<String> refCursor = this.procedures.mappedRefCursor((rs, i) -> i + "-" + rs.getString(1));
    assertEquals(Arrays.asList("0-hello", "1-postgres"), refCursor);
  }

  @Test
  public void mappedRefCursor() {
    List<String> refCursor = this.procedures.mappedRefCursor(rs -> "1-" + rs.getString(1));
    assertEquals(Arrays.asList("1-hello", "1-postgres"), refCursor);
  }

  @Test
  public void mappedRefCursorAndArgumentNumbered() {
    List<String> refCursor = this.procedures.mappedRefCursorAndArgument("prefix-", (rs, i) -> i + "-" + rs.getString(1));
    assertEquals(Arrays.asList("0-prefix-hello", "1-prefix-postgres"), refCursor);
  }

  @Test
  public void mappedRefCursorAndArgument() {
    List<String> refCursor = this.procedures.mappedRefCursorAndArgument("prefix-", rs -> "1-" + rs.getString(1));
    assertEquals(Arrays.asList("1-prefix-hello", "1-prefix-postgres"), refCursor);
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
  public void concatenateTwoArrays() {
    Integer[] result = this.procedures.concatenateTwoArrays(new Integer[] {1, 2, 3}, new Integer[] {4, 5, 6});
    assertArrayEquals(new Integer[] {1, 2, 3, 4, 5, 6}, result);
  }

  @Test
  public void arrayReturnValuePrimitive() {
    int[] result = this.procedures.arrayReturnValuePrimitive();
    assertArrayEquals(new int[] {1, 2, 3, 4}, result);
  }

  @Test
  public void arrayReturnValueRefernce() {
    Integer[] result = this.procedures.arrayReturnValueRefernce();
    assertArrayEquals(new Integer[] {1, 2, 3, 4}, result);
  }

}
