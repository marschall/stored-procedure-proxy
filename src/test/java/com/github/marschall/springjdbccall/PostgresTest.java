package com.github.marschall.springjdbccall;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

@ContextConfiguration(classes = {PostgresConfiguration.class, TestConfiguration.class})
public class PostgresTest {

  @ClassRule
  public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

  @Rule
  public final SpringMethodRule springMethodRule = new SpringMethodRule();

  @Autowired
  private DataSource dataSource;

  private PostgresProcedures procedures;

  @Before
  public void setUp() {
    this.procedures = ProcedureCallerFactory.of(PostgresProcedures.class, this.dataSource)
            .build();

    EncodedResource resource = new EncodedResource(new ClassPathResource("postgres_procedures.sql"), UTF_8);
    DatabasePopulator populator = new PostgresDatabasePopulator("LANGUAGE plpgsql;", resource);
    DatabasePopulatorUtils.execute(populator, this.dataSource);
  }

  @Test
  public void browserVersion() {
    assertEquals("Servo/0.0.1", this.procedures.browserVersion("Servo", "0.0.1"));
  }

  @Test
  public void salesTax() {
    assertEquals(0.01d, 6.0d, this.procedures.salesTax(100.0d));
  }

  @Test
  public void propertyTax() {
    assertEquals(0.01d, 6.0d, this.procedures.propertyTax(100.0d));
  }

}
