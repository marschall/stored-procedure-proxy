package com.github.marschall.storedprocedureproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Disabled;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.transaction.annotation.Transactional;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration;
import com.github.marschall.storedprocedureproxy.configuration.Db2Configuration;
import com.github.marschall.storedprocedureproxy.procedures.Db2Procedures;

@DisabledOnTravis
@Transactional
@ContextConfiguration(classes = Db2Configuration.class)
@Sql(scripts = "classpath:sql/db2_procedures.sql", config = @SqlConfig(separator = "/"))
public class Db2Test extends AbstractDataSourceTest {

  private Db2Procedures procedures(ParameterRegistration parameterRegistration) {
    return ProcedureCallerFactory.of(Db2Procedures.class, this.getDataSource())
            .withParameterRegistration(parameterRegistration)
            .build();
  }

  @Disabled("function calling seems to be broken on DB2")
  @IndexedParametersRegistrationTest
  public void salesTax(ParameterRegistration parameterRegistration) {
    assertEquals(0.01f, 6.0f, this.procedures(parameterRegistration).salesTax(100.0f));
  }

  @AllParametersRegistrationTest
  public void propertyTax(ParameterRegistration parameterRegistration) {
    assertEquals(0.01f, 6.0f, this.procedures(parameterRegistration).propertyTax(100.0f));
  }

}
