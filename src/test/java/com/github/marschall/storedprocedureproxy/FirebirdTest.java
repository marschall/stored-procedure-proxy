package com.github.marschall.storedprocedureproxy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import com.github.marschall.storedprocedureproxy.configuration.FirebirdConfiguration;

@ContextConfiguration(classes = FirebirdConfiguration.class)
@Sql(scripts = "classpath:firebird_procedures.sql", config = @SqlConfig(separator = "^"))
//@Ignore("not availale on Travis")
public class FirebirdTest extends AbstractDataSourceTest {

  @Test
  public void select() throws SQLException {
    try (Connection connection = this.getDataSource().getConnection();
         PreparedStatement statement = connection.prepareStatement("SELECT 1 from RDB$DATABASE");
         ResultSet resultSet = statement.executeQuery()) {
      while (resultSet.next()) {
        System.out.println(resultSet.getInt(1));
      }

    }
  }

}
