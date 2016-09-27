package com.github.marschall.storedprocedureproxy;

import static org.junit.Assert.assertEquals;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

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

import com.github.marschall.storedprocedureproxy.configuration.H2Configuration;
import com.github.marschall.storedprocedureproxy.configuration.TestConfiguration;
import com.github.marschall.storedprocedureproxy.procedures.H2Procedures;
import com.github.marschall.storedprocedureproxy.spi.NamingStrategy;

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

  @Before
  public void setUp() {
    this.procedures = ProcedureCallerFactory.of(H2Procedures.class, this.dataSource)
            .withProcedureNamingStrategy(NamingStrategy.snakeCase().thenUpperCase())
            .build();
  }

  @Test
  public void nativeCall() throws SQLException {
    try (Connection connection = dataSource.getConnection();
            CallableStatement statement = connection.prepareCall("{call STRING_PROCEDURE(?)}")) {
      String input = "test";
      statement.setObject(1, input);

      try (ResultSet rs = statement.executeQuery()) {
        while (rs.next()) {
          assertEquals("pre" + input + "post", rs.getString(1));
        }
      }
    }
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

}
