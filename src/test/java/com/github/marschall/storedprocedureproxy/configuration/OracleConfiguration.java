package com.github.marschall.storedprocedureproxy.configuration;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

@Configuration
public class OracleConfiguration {

  @Bean
  public DataSource dataSource() {
    oracle.jdbc.OracleDriver.isDebug();
    SingleConnectionDataSource dataSource = new SingleConnectionDataSource();
    dataSource.setSuppressClose(true);
    dataSource.setUrl("jdbc:oracle:thin:@localhost:1521/ORCLPDB1");
    dataSource.setUsername("jdbc");
    dataSource.setPassword("Cent-Quick-Space-Bath-8");
    Properties connectionProperties = new Properties();
    connectionProperties.put("oracle.net.disableOob", "true");
    dataSource.setConnectionProperties(connectionProperties);
    return dataSource;
  }

}
