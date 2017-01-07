package com.github.marschall.storedprocedureproxy;

import static com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration.INDEX_AND_TYPE;
import static com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration.INDEX_ONLY;
import static com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration.NAME_AND_TYPE;
import static com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration.NAME_ONLY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration;
import com.github.marschall.storedprocedureproxy.configuration.MssqlConfiguration;
import com.github.marschall.storedprocedureproxy.procedures.MssqlProcedures;

@RunWith(Parameterized.class)
@Sql("classpath:mssql_procedures.sql")
@ContextConfiguration(classes = MssqlConfiguration.class)
@Ignore
public class MssqlTest extends AbstractDataSourceTest {

  private MssqlProcedures procedures;

  private ParameterRegistration parameterRegistration;

  public MssqlTest(ParameterRegistration parameterRegistration) {
    this.parameterRegistration = parameterRegistration;
  }

  @Before
  public void setUp() {
    this.procedures = ProcedureCallerFactory.of(MssqlProcedures.class, this.getDataSource())
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
  public void plus1inout() {
    assertEquals(2, this.procedures.plus1inout(1));
  }

  @Test
  public void plus1inret() {
    assumeTrue(this.parameterRegistration != NAME_ONLY && this.parameterRegistration != NAME_AND_TYPE);
    assertEquals(2, this.procedures.plus1inret(1));
  }

  @Test
  @Ignore
  public void fakeCursor() {
    assertEquals(Arrays.asList("hello", "world"), this.procedures.fakeCursor());
  }

  @Test
  @Ignore
  public void cursorOutParameter() throws SQLException {
    try (Connection connection = this.getDataSource().getConnection();
         Statement statement = connection.createStatement()) {
      statement.execute("DROP PROCEDURE IF EXISTS simpleCursor");
      statement.execute("CREATE PROCEDURE simpleCursor\n"
+ "    @OutputCursor CURSOR VARYING OUTPUT\n"
+ "AS\n"
+ "BEGIN\n"
+ "    SET @OutputCursor = CURSOR\n"
+ "    FORWARD_ONLY STATIC FOR\n"
+ "    SELECT 'hello' UNION ALL SELECT 'world';\n"
+ "\n"
+ "    OPEN @OutputCursor;\n"
+ "END");
      try (CallableStatement call = connection.prepareCall("{ call simpleCursor(?)}")) {
        call.registerOutParameter(1, Types.REF_CURSOR);
        call.execute();
        try (ResultSet resultSet = call.getObject(1, ResultSet.class)) {
          while (resultSet.next()) {
            System.out.println(resultSet.getObject(1, String.class));
          }
        }
      }
//      List<String> values = new ArrayList<>(2);
//      while (resultSet.next()) {
//        values.add(resultSet.getString(1));
//      }
//      assertEquals(Arrays.asList("hello", "world"), values);
    }
  }

}
