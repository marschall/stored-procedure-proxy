Stored Procedure Proxy [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.marschall/stored-procedure-proxy/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.marschall/stored-procedure-proxy) [![Javadocs](http://www.javadoc.io/badge/com.github.marschall/stored-procedure-proxy.svg)](http://www.javadoc.io/doc/com.github.marschall/stored-procedure-proxy) [![Build Status](https://travis-ci.org/marschall/stored-procedure-proxy.svg?branch=master)](https://travis-ci.org/marschall/stored-procedure-proxy)
======================

A more convenient and type safe way to call stored procedures from Java.

This project allows you to define a Java interface method for every stored procedure you want to call. Then it creates a dynamic instance of that interface that calls the stored procedure whenever you call the method.

Simply create an interface that represents the stored procedures you want to call.

```java
public interface TaxProcedures {

  BigDecimal salesTax(BigDecimal subtotal);

}

```

Then create an instance using only a `javax.sql.DataSource`

```java
TaxProcedures taxProcedures = ProcedureCallerFactory.build(TaxProcedures.class, dataSource);

```

Invoking interface methods will then call stored procedure.

```java
taxProcedures.salesTax(new BigDecimal("100.00"));
```

will actually call the stored procedure.


Check out the [wiki](https://github.com/marschall/stored-procedure-proxy/wiki) for more information.

```xml
<dependency>
  <groupId>com.github.marschall</groupId>
  <artifactId>stored-procedure-proxy</artifactId>
  <version>0.1.1</version>
</dependency>
```


What problem does this project solve?
-------------------------------------

Calling simple stored procedures in JDBC or JPA is unnecessarily [cumbersome](https://blog.jooq.org/2016/06/08/using-stored-procedures-with-jpa-jdbc-meh-just-use-jooq/) and not type safe. While this may be required in rare cases in common cases this can be solved much easier. None of the common data access layers solve this issue:

- While [spring-jdbc](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/jdbc.html) offers many ways to call a stored procedure all of them require the registration of [SqlParameter](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/jdbc/core/SqlParameter.html) objects and still access database metadata by default. Metadata access has to be [disabled](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/jdbc/core/simple/SimpleJdbcCall.html#withoutProcedureColumnMetaDataAccess--) for every use. The options are:
 - call [JdbcOperations#cal](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/jdbc/core/JdbcOperations.html#call-org.springframework.jdbc.core.CallableStatementCreator-java.util.List-)
 - sublcass [StoredProcedure](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/jdbc/object/StoredProcedure.html)
 - use [SimpleJdbcCall](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/jdbc/core/simple/SimpleJdbcCall.html)
- [Spring Data JPA](https://github.com/spring-projects/spring-data-examples/tree/master/jpa/jpa21) is close.
- [jOOQ](http://www.jooq.org/doc/3.8/manual/sql-execution/stored-procedures/) requires a class for every call, passing a configuration object and calling setters.
- [jDBI](https://github.com/jdbi/jdbi/issues/135) falls back to manual parameter registration for out parameters as well.
- [Querydsl](https://github.com/querydsl/querydsl/issues/15) has no support at all

While they all have their use case none of them fitted our needs.

