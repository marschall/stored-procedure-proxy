Stored Procedure Proxy [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.marschall/stored-procedure-proxy/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.marschall/stored-procedure-proxy) [![Javadocs](http://www.javadoc.io/badge/com.github.marschall/stored-procedure-proxy.svg)](http://www.javadoc.io/doc/com.github.marschall/stored-procedure-proxy) [![Build Status](https://travis-ci.org/marschall/stored-procedure-proxy.svg?branch=master)](https://travis-ci.org/marschall/stored-procedure-proxy)  [![license](https://img.shields.io/github/license/mashape/apistatus.svg?maxAge=2592000)](https://opensource.org/licenses/MIT)
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

The project has no runtime dependencies and is a single JAR below 100 kB.

```xml
<dependency>
  <groupId>com.github.marschall</groupId>
  <artifactId>stored-procedure-proxy</artifactId>
  <version>0.6.0</version>
</dependency>
```


What problem does this project solve?
-------------------------------------

Calling simple stored procedures in JDBC or JPA is unnecessarily [cumbersome](https://blog.jooq.org/2016/06/08/using-stored-procedures-with-jpa-jdbc-meh-just-use-jooq/) and not type safe. While this may be required in rare cases in common cases this can be solved much easier. None of the common data access layers solve this issue:

- While [spring-jdbc](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/jdbc.html) offers many ways to call a stored procedure all of them require the registration of [SqlParameter](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/jdbc/core/SqlParameter.html) objects. The options are:
  - call [JdbcOperations#cal](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/jdbc/core/JdbcOperations.html#call-org.springframework.jdbc.core.CallableStatementCreator-java.util.List-)
  - sublcass [StoredProcedure](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/jdbc/object/StoredProcedure.html)
  - use [GenericStoredProcedure](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/jdbc/object/GenericStoredProcedure.html)
  - use [SimpleJdbcCall](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/jdbc/core/simple/SimpleJdbcCall.html), accesses database metadata by default. Metadata access has to be [disabled](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/jdbc/core/simple/SimpleJdbcCall.html#withoutProcedureColumnMetaDataAccess--) for every use
- [Spring Data JPA](https://github.com/spring-projects/spring-data-examples/tree/master/jpa/jpa21) offers two ways
  - the first is hardly an improvement since it still needs a `@NamedStoredProcedureQuery`
  - the [second](https://jira.spring.io/browse/DATAJPA-455) is quite nice, we take inspiration from this approach and add more flexibility
- [jOOQ](http://www.jooq.org/doc/3.8/manual/sql-execution/stored-procedures/) offers stored procedure support in a way that is similar to this project, in addition it supports many more features and can generate classes from a database schema. The only down sides are that it requires passing a configuration object ([for now](https://github.com/jOOQ/jOOQ/issues/5677)) and Oracle support is commercial.
- [jDBI](https://github.com/jdbi/jdbi/issues/135) falls back to manual parameter registration for out parameters as well.
- [Ebean](https://ebean-orm.github.io/apidocs/com/avaje/ebean/CallableSql.html) falls back to manual parameter registration for out parameters as well.
- [Querydsl](https://github.com/querydsl/querydsl/issues/15) has no support at all
- [Sql2o](https://groups.google.com/forum/#!topic/sql2o/4Fdh5VjZ-uk) seems to have no support at all
- [spwrap](https://github.com/mhewedy/spwrap) is similar in spirit but requires more annotations and is currently a bit less flexible

While they all have their use case none of them fitted our needs.

