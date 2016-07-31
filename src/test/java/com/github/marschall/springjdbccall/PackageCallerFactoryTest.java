package com.github.marschall.springjdbccall;

import static org.junit.Assert.assertEquals;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

@ContextConfiguration(classes = DataSourceConfiguration.class)
public class PackageCallerFactoryTest {

  @ClassRule
  public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

  @Rule
  public final SpringMethodRule springMethodRule = new SpringMethodRule();

  @Autowired
  private DataSource dataSource;

  @Autowired
  private JdbcOperations jdbcOperations;

  @Test
  public void nativeCall() throws SQLException {
    try (Connection connection = dataSource.getConnection();
            CallableStatement statement = connection.prepareCall("{call STRING_FUNCTION(?)}")) {
      String input = "test";
      statement.setObject(1, input);

      try (ResultSet rs = statement.executeQuery()) {
        while (rs.next()) {
          assertEquals("pre" + input + "post", rs.getString(1));
        }
      }

//      if (statement.execute()) {
//        System.out.println(true);
//        statement.getRe
//      } else {
//        System.out.println(false);
//        statement.getUpdateCount();
//      }

    }
  }

  @Test
  public void proxyCall() {
    H2Functions functions = PackageCallerFactory.of(H2Functions.class, jdbcOperations).build();

    String input = "test";
    assertEquals("pre" + input + "post", functions.stringFunction(input));
  }

}
