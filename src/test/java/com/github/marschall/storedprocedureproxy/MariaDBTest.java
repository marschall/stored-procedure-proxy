package com.github.marschall.storedprocedureproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration;
import com.github.marschall.storedprocedureproxy.configuration.MariaDBConfiguration;
import com.github.marschall.storedprocedureproxy.procedures.MariaDBProcedures;

@ContextConfiguration(classes = MariaDBConfiguration.class)
@Sql("classpath:sql/mariadb_procedures.sql")
@DisabledOnTravis
public class MariaDBTest extends AbstractDataSourceTest {

  private MariaDBProcedures procedures(ParameterRegistration parameterRegistration) {
    return ProcedureCallerFactory.of(MariaDBProcedures.class, this.getDataSource())
            .withParameterRegistration(parameterRegistration)
            .build();
  }

  @IndexedParametersRegistrationTest
  public void helloFunction(ParameterRegistration parameterRegistration) {
    // names for out parameters of functions don't work

    assertEquals("Hello, Monty!", this.procedures(parameterRegistration).helloFunction("Monty"));
  }

  @IndexedParametersRegistrationTest
  public void helloProcedure(ParameterRegistration parameterRegistration) {
    assertEquals("Hello, Monty!", this.procedures(parameterRegistration).helloProcedure("Monty"));
  }

  @AllParametersRegistrationTest
  public void simpleRefCursor(ParameterRegistration parameterRegistration) {
    // https://stackoverflow.com/questions/273929/what-is-the-equivalent-of-oracle-s-ref-cursor-in-mysql-when-using-jdbc
    List<String> refCursor = this.procedures(parameterRegistration).fakeRefcursor();
    assertEquals(Arrays.asList("hello", "mysql"), refCursor);
  }

}
