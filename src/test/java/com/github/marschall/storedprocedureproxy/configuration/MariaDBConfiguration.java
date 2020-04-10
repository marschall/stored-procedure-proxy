package com.github.marschall.storedprocedureproxy.configuration;

import javax.sql.DataSource;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

@Configuration
public class MariaDBConfiguration {

  @Bean
  public DataSource dataSource() {
    try {
      Class.forName("org.mariadb.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      throw new BeanCreationException("mariadb driver not present", e);
    }
    SingleConnectionDataSource dataSource = new SingleConnectionDataSource();
    dataSource.setSuppressClose(true);
    // https://mariadb.com/kb/en/mariadb/about-mariadb-connector-j/
    dataSource.setUrl("jdbc:mariadb://localhost:3307/jdbc");
    dataSource.setUsername("jdbc");
    dataSource.setPassword("Cent-Quick-Space-Bath-8");
    return dataSource;
  }

}
