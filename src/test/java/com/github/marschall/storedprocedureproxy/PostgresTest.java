package com.github.marschall.storedprocedureproxy;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.postgresql.util.PSQLException;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.transaction.annotation.Transactional;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration;
import com.github.marschall.storedprocedureproxy.configuration.PostgresConfiguration;
import com.github.marschall.storedprocedureproxy.procedures.PostgresProcedures;

@Transactional
@ContextConfiguration(classes = PostgresConfiguration.class)
@Sql(scripts = "classpath:postgres_procedures.sql", config = @SqlConfig(separator = "@"))
public class PostgresTest extends AbstractDataSourceTest {

  private PostgresProcedures procedures(ParameterRegistration parameterRegistration) {
    return ProcedureCallerFactory.of(PostgresProcedures.class, this.getDataSource())
            .withParameterRegistration(parameterRegistration)
            .withPostgresArrays()
            .build();
  }

  @ParameterizedTest
  @IndexRegistrationParameters
  public void browserVersion(ParameterRegistration parameterRegistration) {
    assertEquals("Servo/0.0.1", this.procedures(parameterRegistration).browserVersion("Servo", "0.0.1"));
  }

  @ParameterizedTest
  @IndexRegistrationParameters
  public void salesTax(ParameterRegistration parameterRegistration) {
    assertEquals(0.01f, 6.0f, this.procedures(parameterRegistration).salesTax(100.0f));
  }

  @ParameterizedTest
  @IndexRegistrationParameters
  public void propertyTax(ParameterRegistration parameterRegistration) {
    assertEquals(0.01f, 6.0f, this.procedures(parameterRegistration).propertyTax(100.0f));
  }

  @ParameterizedTest
  @IndexRegistrationParameters
  public void raiseCheckedException(ParameterRegistration parameterRegistration) {
    SQLException sqlException = assertThrows(SQLException.class, () -> this.procedures(parameterRegistration).raiseCheckedException());
    assertTrue(sqlException instanceof PSQLException);
    assertEquals("22000", sqlException.getSQLState());
  }

  @ParameterizedTest
  @IndexRegistrationParameters
  public void raiseUncheckedException(ParameterRegistration parameterRegistration) {
    DataAccessException e = assertThrows(DataAccessException.class, () -> this.procedures(parameterRegistration).raiseUncheckedException());
    Throwable cause = e.getCause();
    assertTrue(cause instanceof PSQLException);
    assertEquals("22000", ((SQLException) cause).getSQLState());
  }

  @ParameterizedTest
  @IndexRegistrationParameters
  public void simpleRefCursor(ParameterRegistration parameterRegistration) {
    List<String> refCursor = this.procedures(parameterRegistration).simpleRefCursor();
    assertEquals(Arrays.asList("hello", "postgres"), refCursor);
  }

  @ParameterizedTest
  @IndexRegistrationParameters
  public void simpleRefCursorOut(ParameterRegistration parameterRegistration) {
    List<String> refCursor = this.procedures(parameterRegistration).simpleRefCursorOut();
    assertEquals(Arrays.asList("hello", "postgres"), refCursor);
  }

  @ParameterizedTest
  @IndexRegistrationParameters
  public void mappedRefCursorNumbered(ParameterRegistration parameterRegistration) {
    List<String> refCursor = this.procedures(parameterRegistration).mappedRefCursor((rs, i) -> i + "-" + rs.getString(1));
    assertEquals(Arrays.asList("0-hello", "1-postgres"), refCursor);
  }

  @ParameterizedTest
  @IndexRegistrationParameters
  public void mappedRefCursor(ParameterRegistration parameterRegistration) {
    List<String> refCursor = this.procedures(parameterRegistration).mappedRefCursor(rs -> "1-" + rs.getString(1));
    assertEquals(Arrays.asList("1-hello", "1-postgres"), refCursor);
  }

  @ParameterizedTest
  @IndexRegistrationParameters
  public void mappedRefCursorAndArgumentNumbered(ParameterRegistration parameterRegistration) {
    List<String> refCursor = this.procedures(parameterRegistration).mappedRefCursorAndArgument("prefix-", (rs, i) -> i + "-" + rs.getString(1));
    assertEquals(Arrays.asList("0-prefix-hello", "1-prefix-postgres"), refCursor);
  }

  @ParameterizedTest
  @IndexRegistrationParameters
  public void mappedRefCursorAndArgument(ParameterRegistration parameterRegistration) {
    List<String> refCursor = this.procedures(parameterRegistration).mappedRefCursorAndArgument("prefix-", rs -> "1-" + rs.getString(1));
    assertEquals(Arrays.asList("1-prefix-hello", "1-prefix-postgres"), refCursor);
  }

  @ParameterizedTest
  @IndexRegistrationParameters
  public void sampleArrayArgumentList(ParameterRegistration parameterRegistration) {
    String result = this.procedures(parameterRegistration).sampleArrayArgumentList(Arrays.asList(1, 2, 3));
    assertEquals("1, 2, 3", result);
  }

  @ParameterizedTest
  @IndexRegistrationParameters
  public void sampleArrayArgumentArray(ParameterRegistration parameterRegistration) {
    String result = this.procedures(parameterRegistration).sampleArrayArgumentArray(new Integer[] {1, 2, 3});
    assertEquals("1, 2, 3", result);
  }

  @ParameterizedTest
  @IndexRegistrationParameters
  public void sampleArrayArgumentPrimitiveArray(ParameterRegistration parameterRegistration) {
    String result = this.procedures(parameterRegistration).sampleArrayArgumentPrimitiveArray(new int[] {1, 2, 3});
    assertEquals("1, 2, 3", result);
  }

  @ParameterizedTest
  @IndexRegistrationParameters
  public void concatenateTwoArrays(ParameterRegistration parameterRegistration) {
    Integer[] result = this.procedures(parameterRegistration).concatenateTwoArrays(new Integer[] {1, 2, 3}, new Integer[] {4, 5, 6});
    assertArrayEquals(new Integer[] {1, 2, 3, 4, 5, 6}, result);
  }

  @ParameterizedTest
  @IndexRegistrationParameters
  public void arrayReturnValuePrimitive(ParameterRegistration parameterRegistration) {
    int[] result = this.procedures(parameterRegistration).arrayReturnValuePrimitive();
    assertArrayEquals(new int[] {1, 2, 3, 4}, result);
  }

  @ParameterizedTest
  @IndexRegistrationParameters
  public void arrayReturnValueRefernce(ParameterRegistration parameterRegistration) {
    Integer[] result = this.procedures(parameterRegistration).arrayReturnValueRefernce();
    assertArrayEquals(new Integer[] {1, 2, 3, 4}, result);
  }

}
