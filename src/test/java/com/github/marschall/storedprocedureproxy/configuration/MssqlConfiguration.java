package com.github.marschall.storedprocedureproxy.configuration;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

@Configuration
public class MssqlConfiguration {

  @Bean
  public DataSource dataSource() {
    try {
      if (!com.microsoft.sqlserver.jdbc.SQLServerDriver.isRegistered()) {
        com.microsoft.sqlserver.jdbc.SQLServerDriver.register();
      }
    } catch (SQLException e) {
      throw new BeanCreationException("could not register driver", e);
    }
    SingleConnectionDataSource dataSource = new SingleConnectionDataSource();
    dataSource.setSuppressClose(true);
    dataSource.setUrl("jdbc:sqlserver://localhost:1433;databaseName=master");
    dataSource.setUsername("sa");
    dataSource.setPassword("Cent-Quick-Space-Bath-8");
    return dataSource;
  }

}