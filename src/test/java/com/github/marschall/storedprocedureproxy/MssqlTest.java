package com.github.marschall.storedprocedureproxy;

import static com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration.INDEX_AND_TYPE;
import static com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration.INDEX_ONLY;
import static com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration.NAME_AND_TYPE;
import static com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration.NAME_ONLY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

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
import com.github.marschall.storedprocedureproxy.spi.NamingStrategy;

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
            .withProcedureNamingStrategy(NamingStrategy.snakeCase().thenUpperCase())
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

}
