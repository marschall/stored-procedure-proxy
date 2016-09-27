package com.github.marschall.storedprocedureproxy;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

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

import com.github.marschall.storedprocedureproxy.configuration.DerbyConfiguration;
import com.github.marschall.storedprocedureproxy.configuration.TestConfiguration;
import com.github.marschall.storedprocedureproxy.procedures.DerbyProcedures;

@Transactional
@ContextConfiguration(classes = {DerbyConfiguration.class, TestConfiguration.class})
public class DerbyTest {

  @ClassRule
  public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

  @Rule
  public final SpringMethodRule springMethodRule = new SpringMethodRule();

  @Autowired
  private DataSource dataSource;

  private DerbyProcedures functions;

  @Before
  public void setUp() {
    this.functions = ProcedureCallerFactory.of(DerbyProcedures.class, this.dataSource)
            .withNamespace()
            .build();
  }

  @Test
  public void outParameter() {
    assertEquals(0, new BigDecimal(201609).compareTo(functions.calculateRevenueByMonth(9, 2016)));
  }

  @Test
  public void returnValue() {
    assertEquals(0.01d, 6.0d, functions.salesTax(100.0d));
  }

}
