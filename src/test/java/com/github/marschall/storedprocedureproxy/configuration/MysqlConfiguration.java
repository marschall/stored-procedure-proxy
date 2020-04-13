package com.github.marschall.storedprocedureproxy.configuration;

import javax.sql.DataSource;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import com.github.marschall.storedprocedureproxy.Travis;

@Configuration
public class MysqlConfiguration {

  @Bean
  public DataSource dataSource() {
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      throw new BeanCreationException("mysql driver not present", e);
    }
    SingleConnectionDataSource dataSource = new SingleConnectionDataSource();
    dataSource.setSuppressClose(true);
    String userName = System.getProperty("user.name");
    String database = userName;
    // https://dev.mysql.com/doc/connector-j/6.0/en/connector-j-reference-configuration-properties.html
    dataSource.setUrl("jdbc:mysql://localhost:3306/" + database + "?useSSL=false&allowPublicKeyRetrieval=true&logger=com.mysql.cj.log.Slf4JLogger");
    dataSource.setUsername(userName);
    String password = Travis.isTravis() ? "" : userName;
    dataSource.setPassword(password);
    return dataSource;
  }

}
