package com.github.marschall.storedprocedureproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.transaction.annotation.Transactional;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration;
import com.github.marschall.storedprocedureproxy.configuration.OracleConfiguration;
import com.github.marschall.storedprocedureproxy.procedures.OracleProcedures;

@DisabledOnTravis
@Transactional
@ContextConfiguration(classes = OracleConfiguration.class)
@Sql(scripts = "classpath:sql/oracle_procedures.sql", config = @SqlConfig(separator = "/"))
public class OracleTest extends AbstractDataSourceTest {

  private OracleProcedures procedures(ParameterRegistration parameterRegistration) {
    return ProcedureCallerFactory.of(OracleProcedures.class, this.getDataSource())
            .withParameterRegistration(parameterRegistration)
            .withOracleExtensions()
            .build();
  }

  @IndexedParametersRegistrationTest
  public void salesTax(ParameterRegistration parameterRegistration) {
    assertEquals(0.01f, 6.0f, this.procedures(parameterRegistration).salesTax(100.0f));
  }

  @ParameterizedTest
  @EnumSource(value = ParameterRegistration.class, names = {"INDEX_ONLY", "INDEX_AND_TYPE", "NAME_ONLY", "NAME_AND_TYPE"})
  public void propertyTax(ParameterRegistration parameterRegistration) {
    assertEquals(0.01f, 6.0f, this.procedures(parameterRegistration).propertyTax(100.0f));
  }

}
