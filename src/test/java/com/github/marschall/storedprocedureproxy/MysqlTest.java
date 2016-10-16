package com.github.marschall.storedprocedureproxy;

import static com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration.INDEX_AND_TYPE;
import static com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration.INDEX_ONLY;
import static com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration.NAME_ONLY;
import static com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration.NAME_AND_TYPE;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import static org.junit.Assume.assumeFalse;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.transaction.annotation.Transactional;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration;
import com.github.marschall.storedprocedureproxy.configuration.MysqlConfiguration;
import com.github.marschall.storedprocedureproxy.configuration.TestConfiguration;
import com.github.marschall.storedprocedureproxy.procedures.MysqlProcedures;

@RunWith(Parameterized.class)
@Transactional
@Sql("classpath:mysql_procedures.sql")
@ContextConfiguration(classes = {MysqlConfiguration.class, TestConfiguration.class})
public class MysqlTest {

  @ClassRule
  public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

  @Rule
  public final SpringMethodRule springMethodRule = new SpringMethodRule();

  @Autowired
  private DataSource dataSource;

  private MysqlProcedures procedures;

  private ParameterRegistration parameterRegistration;

  public MysqlTest(ParameterRegistration parameterRegistration) {
    this.parameterRegistration = parameterRegistration;
  }

  @Before
  public void setUp() {
    this.procedures = ProcedureCallerFactory.of(MysqlProcedures.class, this.dataSource)
            .withParameterRegistration(this.parameterRegistration)
            .build();
  }

  @Parameters
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
    assertEquals("Hello, Monty!", this.procedures.helloProcedure("Monty"));
  }

  @Test
  public void simpleRefCursor() {
    // https://stackoverflow.com/questions/273929/what-is-the-equivalent-of-oracle-s-ref-cursor-in-mysql-when-using-jdbc
    List<String> refCursor = this.procedures.fakeRefcursor();
    assertEquals(Arrays.asList("hello", "mysql"), refCursor);
  }

}
