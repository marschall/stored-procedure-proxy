package com.github.marschall.springjdbccall;

import static org.junit.Assert.assertEquals;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

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

  @Before
  public void setUp() {
    this.procedures = ProcedureCallerFactory.of(MysqlProcedures.class, this.dataSource)
            .build();
  }

  @Test
  public void helloFunction() {
    assertEquals("Hello Monty!", this.procedures.helloFunction("Monty"));
  }

  @Test
  public void helloProcedure() {
    assertEquals("Hello Monty!", this.procedures.helloProcedure("Monty"));
  }

}
