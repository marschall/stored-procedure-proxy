package com.github.marschall.storedprocedureproxy.configuration;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

@Configuration
public class FirebirdConfiguration {


  @Bean
  public DataSource dataSource() {
    SingleConnectionDataSource dataSource = new SingleConnectionDataSource();
    dataSource.setSuppressClose(true);
    // https://www.firebirdsql.org/file/documentation/drivers_documentation/java/faq.html#jdbc-urls-java.sql.drivermanager
    dataSource.setUrl("jdbc:firebirdsql://localhost:3050");
    // https://github.com/almeida/docker-firebird
    dataSource.setUsername("SYSDBA");
    dataSource.setPassword("masterkey");
    return dataSource;
  }

}
