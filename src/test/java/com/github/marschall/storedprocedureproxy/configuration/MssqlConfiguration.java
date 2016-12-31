package com.github.marschall.storedprocedureproxy.configuration;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

@Configuration
public class MssqlConfiguration {

  @Bean
  public DataSource dataSource() {
    SingleConnectionDataSource dataSource = new SingleConnectionDataSource();
    dataSource.setSuppressClose(true);
    // defaults from Postgres.app
//    dataSource.setUrl("jdbc:sqlserver://localhost:1433;databaseName=master;user=sa;password=your_password");
    dataSource.setUrl("jdbc:sqlserver://localhost:1433;databaseName=master");
    dataSource.setUsername("sa");
    dataSource.setPassword("Cent-Quick-Space-Bath-8");
    return dataSource;
  }

}