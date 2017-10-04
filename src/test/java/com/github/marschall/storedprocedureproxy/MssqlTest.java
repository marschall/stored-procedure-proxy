package com.github.marschall.storedprocedureproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.params.ParameterizedTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration;
import com.github.marschall.storedprocedureproxy.configuration.MssqlConfiguration;
import com.github.marschall.storedprocedureproxy.procedures.MssqlProcedures;

@Sql("classpath:mssql_procedures.sql")
@ContextConfiguration(classes = MssqlConfiguration.class)
@DisabledOnTravis
public class MssqlTest extends AbstractDataSourceTest {

  private MssqlProcedures procedures(ParameterRegistration parameterRegistration) {
    return ProcedureCallerFactory.of(MssqlProcedures.class, this.getDataSource())
            .withParameterRegistration(parameterRegistration)
            .build();
  }

  @ParameterizedTest
  @IndexRegistrationParameters
  public void plus1inout(ParameterRegistration parameterRegistration) {
    assertEquals(2, this.procedures(parameterRegistration).plus1inout(1));
  }

  @ParameterizedTest
  @IndexRegistrationParameters
  public void plus1inret(ParameterRegistration parameterRegistration) {
    assertEquals(2, this.procedures(parameterRegistration).plus1inret(1));
  }

  @ParameterizedTest
  @IndexRegistrationParameters
  public void fakeCursor(ParameterRegistration parameterRegistration) {
    assertEquals(Arrays.asList("hello", "world"), this.procedures(parameterRegistration).fakeCursor());
  }

}
