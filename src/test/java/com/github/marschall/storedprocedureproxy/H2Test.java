package com.github.marschall.storedprocedureproxy;

import static com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration.INDEX_AND_TYPE;
import static com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration.INDEX_ONLY;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.transaction.annotation.Transactional;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration;
import com.github.marschall.storedprocedureproxy.configuration.H2Configuration;
import com.github.marschall.storedprocedureproxy.configuration.TestConfiguration;
import com.github.marschall.storedprocedureproxy.procedures.H2Procedures;
import com.github.marschall.storedprocedureproxy.spi.NamingStrategy;

@RunWith(Parameterized.class)
@Transactional
@ContextConfiguration(classes = {H2Configuration.class, TestConfiguration.class})
public class H2Test {

  @ClassRule
  public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

  @Rule
  public final SpringMethodRule springMethodRule = new SpringMethodRule();

  @Autowired
  private DataSource dataSource;

  private H2Procedures procedures;

  private ParameterRegistration parameterRegistration;

  public H2Test(ParameterRegistration parameterRegistration) {
    this.parameterRegistration = parameterRegistration;
  }

  @Before
  public void setUp() {
    this.procedures = ProcedureCallerFactory.of(H2Procedures.class, this.dataSource)
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
  public void callScalarProcedure() {
    String input = "test";
    assertEquals("pre" + input + "post", procedures.stringProcedure(input));
  }

  @Test
  public void callVoidProcedure() {
    procedures.voidProcedure("test");
  }

  @Test
  public void noArgProcedure() {
    assertEquals("output", procedures.noArgProcedure());
  }

}
