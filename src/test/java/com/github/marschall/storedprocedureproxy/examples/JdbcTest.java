package com.github.marschall.storedprocedureproxy.examples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import javax.sql.DataSource;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.transaction.annotation.Transactional;

import com.github.marschall.storedprocedureproxy.configuration.HsqlConfiguration;
import com.github.marschall.storedprocedureproxy.configuration.TestConfiguration;

@Transactional
@ContextConfiguration(classes = {HsqlConfiguration.class, TestConfiguration.class})
public class JdbcTest {

  @ClassRule
  public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

  @Rule
  public final SpringMethodRule springMethodRule = new SpringMethodRule();

  @Autowired
  private DataSource dataSource;

  @Test
  public void call() throws SQLException {
    assertEquals(2, plus1inout(1));
  }

  private int plus1inout(int argument) throws SQLException {
    try (Connection connection = this.dataSource.getConnection();
         CallableStatement statement = connection.prepareCall("{call plus1inout(?, ?)}")) {
        statement.setInt(1, argument);
        statement.registerOutParameter(2, Types.INTEGER);

        assertFalse(statement.execute());
        return statement.getInt(2);
    }
  }

}
