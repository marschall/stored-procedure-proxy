package com.github.marschall.storedprocedureproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration;
import com.github.marschall.storedprocedureproxy.configuration.FirebirdConfiguration;
import com.github.marschall.storedprocedureproxy.procedures.FirebirdProcedures;


@ContextConfiguration(classes = FirebirdConfiguration.class)
@Sql(scripts = "classpath:firebird_procedures.sql", config = @SqlConfig(separator = "^"))
@DisabledOnTravis
public class FirebirdTest extends AbstractDataSourceTest {

  private FirebirdProcedures procedures(ParameterRegistration parameterRegistration) {
    return ProcedureCallerFactory.of(FirebirdProcedures.class, this.getDataSource())
            .withParameterRegistration(parameterRegistration)
            .build();
  }

  @ParameterRegistrationTest
  public void increment(ParameterRegistration parameterRegistration) {
    assertEquals(2, this.procedures(parameterRegistration).increment(1));
  }

  @ParameterRegistrationTest
  public void incrementOutParameter(ParameterRegistration parameterRegistration) {
    assertEquals(2, this.procedures(parameterRegistration).incrementOutParameter(1));
  }

  @ParameterRegistrationTest
  public void incrementReturnValue(ParameterRegistration parameterRegistration) {
    assertEquals(2, this.procedures(parameterRegistration).incrementReturnValue(1));
  }

  @ParameterRegistrationTest
  public void cursor(ParameterRegistration parameterRegistration) {
    assertEquals(Arrays.asList(1, 1, 2, 6, 24, 120), this.procedures(parameterRegistration).factorial(5));
  }

}
