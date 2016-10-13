package com.github.marschall.storedprocedureproxy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;

public class ObjectMethodsTest {

  private DataSource dataSource;

  private Connection connection;

  private SimpleProcedures procedures;

  @Before
  public void setUp() throws SQLException {
    this.dataSource = mock(DataSource.class);
    this.connection = mock(Connection.class);
    DatabaseMetaData metaData = mock(DatabaseMetaData.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.getMetaData()).thenReturn(metaData);
    when(metaData.getDatabaseProductName()).thenReturn("junit");
    when(connection.prepareCall(anyString())).thenThrow(SQLException.class);
    procedures = ProcedureCallerFactory.build(SimpleProcedures.class, this.dataSource);
  }

  @Test
  public void testEquals() {
    assertEquals(this.procedures, this.procedures);
    assertNotEquals(this.procedures, null);
  }

  @Test
  public void testToString() {
    assertThat(this.procedures.toString(), containsString(SimpleProcedures.class.getName()));
  }

  @Test
  public void testHashCode() {
    this.procedures.hashCode();
  }

  interface SimpleProcedures {

  }

}
