package com.github.marschall.storedprocedureproxy;

import static com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration.INDEX_AND_TYPE;
import static com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration.INDEX_ONLY;
import static com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration.NAME_AND_TYPE;
import static com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration.NAME_ONLY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeFalse;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration;
import com.github.marschall.storedprocedureproxy.configuration.MariaDBConfiguration;
import com.github.marschall.storedprocedureproxy.procedures.MariaDBProcedures;

@ContextConfiguration(classes = MariaDBConfiguration.class)
@RunWith(Parameterized.class)
@Sql("classpath:mariadb_procedures.sql")
@Ignore("not availale on Travis")
public class MariaDBTest extends AbstractDataSourceTest {

  private MariaDBProcedures procedures;

  private ParameterRegistration parameterRegistration;

  public MariaDBTest(ParameterRegistration parameterRegistration) {
    this.parameterRegistration = parameterRegistration;
  }

  @Before
  public void setUp() {
    this.procedures = ProcedureCallerFactory.of(MariaDBProcedures.class, this.getDataSource())
            .withParameterRegistration(this.parameterRegistration)
            .build();
  }

  @Parameters(name = "{0}")
  public static Collection<Object[]> parameters() {
    return Arrays.asList(
            new Object[] {INDEX_ONLY},
            new Object[] {INDEX_AND_TYPE},
            new Object[] {NAME_ONLY},
            new Object[] {NAME_AND_TYPE}
    );
  }

  @Test
  public void helloFunction() {
    // names for out parameters of functions don't work
    assumeFalse(this.parameterRegistration == NAME_ONLY);
    assumeFalse(this.parameterRegistration == NAME_AND_TYPE);

    assertEquals("Hello, Monty!", this.procedures.helloFunction("Monty"));
  }

  @Test
  public void helloProcedure() {
    assumeFalse(this.parameterRegistration == NAME_ONLY);
    assumeFalse(this.parameterRegistration == NAME_AND_TYPE);
    assertEquals("Hello, Monty!", this.procedures.helloProcedure("Monty"));
  }

  @Test
  public void simpleRefCursor() {
    // https://stackoverflow.com/questions/273929/what-is-the-equivalent-of-oracle-s-ref-cursor-in-mysql-when-using-jdbc
    List<String> refCursor = this.procedures.fakeRefcursor();
    assertEquals(Arrays.asList("hello", "mysql"), refCursor);
  }

}
