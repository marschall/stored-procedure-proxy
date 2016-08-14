package com.github.marschall.springjdbccall;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

@Configuration
public class MysqlConfiguration {

  @Bean
  public DataSource dataSource() {
    SingleConnectionDataSource dataSource = new SingleConnectionDataSource();
    dataSource.setSuppressClose(true);
    String userName = System.getProperty("user.name");
    String database = userName;
    dataSource.setUrl("jdbc:mysql:///" + database);
    dataSource.setUsername(userName);
    dataSource.setPassword("");
    return dataSource;
  }

}
