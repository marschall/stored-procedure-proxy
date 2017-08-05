package com.github.marschall.storedprocedureproxy;

import static com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration.INDEX_AND_TYPE;
import static com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration.INDEX_ONLY;
import static org.junit.Assert.assertEquals;

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
import org.springframework.test.context.jdbc.SqlConfig;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration;
import com.github.marschall.storedprocedureproxy.configuration.FirebirdConfiguration;
import com.github.marschall.storedprocedureproxy.procedures.FirebirdProcedures;


@RunWith(Parameterized.class)
@ContextConfiguration(classes = FirebirdConfiguration.class)
@Sql(scripts = "classpath:firebird_procedures.sql", config = @SqlConfig(separator = "^"))
@Ignore("not availale on Travis")
public class FirebirdTest extends AbstractDataSourceTest {

  private FirebirdProcedures procedures;

  private ParameterRegistration parameterRegistration;

  public FirebirdTest(ParameterRegistration parameterRegistration) {
    this.parameterRegistration = parameterRegistration;
  }

  @Before
  public void setUp() {
    this.procedures = ProcedureCallerFactory.of(FirebirdProcedures.class, this.getDataSource())
            .withParameterRegistration(this.parameterRegistration)
            .build();
  }

  @Parameters(name = "{0}")
  public static Collection<Object[]> parameters() {
    return Arrays.asList(
            new Object[] {INDEX_ONLY},
            new Object[] {INDEX_AND_TYPE}
     );
  }

  @Test
  public void increment() {
    assertEquals(2, this.procedures.increment(1));
  }

  @Test
  public void incrementOutParameter() {
    assertEquals(2, this.procedures.incrementOutParameter(1));
  }

  @Test
  public void incrementReturnValue() {
    assertEquals(2, this.procedures.incrementReturnValue(1));
  }

  @Test
  public void cursor() {
    assertEquals(Arrays.asList(1, 1, 2, 6, 24, 120), this.procedures.factorial(5));
  }

}
