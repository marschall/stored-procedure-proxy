package com.github.marschall.storedprocedureproxy.configuration;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

@Configuration
public class Db2Configuration {

  @Bean
  public DataSource dataSource() {
    com.ibm.db2.jcc.DB2Driver.getMyClassLoader();
    SingleConnectionDataSource dataSource = new SingleConnectionDataSource();
    dataSource.setSuppressClose(true);
    dataSource.setUrl("jdbc:db2://localhost:50000/jdbc");
    dataSource.setUsername("db2inst1");
    dataSource.setPassword("Cent-Quick-Space-Bath-8");
    return dataSource;
  }

}
