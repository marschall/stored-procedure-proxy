package com.github.marschall.storedprocedureproxy.examples;

import static org.junit.Assert.assertEquals;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.transaction.annotation.Transactional;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory;
import com.github.marschall.storedprocedureproxy.configuration.HsqlConfiguration;
import com.github.marschall.storedprocedureproxy.configuration.TestConfiguration;
import com.github.marschall.storedprocedureproxy.procedures.ComparisonExample;

@Transactional
@ContextConfiguration(classes = {HsqlConfiguration.class, TestConfiguration.class})
public class ComparisonExampleTest {

  @ClassRule
  public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

  @Rule
  public final SpringMethodRule springMethodRule = new SpringMethodRule();

  @Autowired
  private DataSource dataSource;

  private ComparisonExample procedures;

  @Before
  public void setUp() {
    this.procedures = ProcedureCallerFactory.build(ComparisonExample.class, this.dataSource);
  }

  @Test
  public void procedure() {
    assertEquals(2, this.procedures.plus1inout(1));
  }

}
