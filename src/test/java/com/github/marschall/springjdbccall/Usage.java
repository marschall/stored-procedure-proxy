package com.github.marschall.springjdbccall;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.github.marschall.springjdbccall.ProcedureCallerFactory.ParameterRegistration;
import com.github.marschall.springjdbccall.procedures.PostgresProcedures;

public class Usage {

  public static void simpleUsage() throws NamingException {
    DataSource dataSource = (DataSource) new InitialContext().lookup("java:comp/DefaultDataSource");
    Class<PostgresProcedures> inferfaceDeclaration = PostgresProcedures.class;
    PostgresProcedures procedures = ProcedureCallerFactory.build(inferfaceDeclaration, dataSource);
    procedures.browserVersion("safari", "9.0");
  }

  public static void advancedUsage() throws NamingException {
    DataSource dataSource = (DataSource) new InitialContext().lookup("java:comp/DefaultDataSource");
    Class<PostgresProcedures> inferfaceDeclaration = PostgresProcedures.class;
    PostgresProcedures procedures = ProcedureCallerFactory.of(inferfaceDeclaration, dataSource)
            .withParameterRegistration(ParameterRegistration.INDEX_AND_TYPE)
            .build();
    procedures.browserVersion("safari", "9.0");
  }

}
