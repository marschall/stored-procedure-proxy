Stored Procedure Proxy
======================

A more convenient and type safe way to call stored procedures from Java.

This project allows you to define a Java interface method for every stored procedure you want to call. Then it creates a dynamic instance of that interface that calls the stored procedure whenever you call the method.

Features
--------

- avoids JDBC metadata
 - works if the database user is not the schema owner
 - works if there are hundreds of visible schemas
- works with Oracle packages
- supports different ways of retrieving results
 - procedures with out parameters
 - functions with out parameters
 - database drivers that return `ResultSet`s
- names for schemas, procedures and parameters can be supplied explicitly or derived if you have a naming convention
 - supports binding by parameter names
 - parameter names can be read from [source](https://docs.oracle.com/javase/tutorial/reflect/member/methodparameterreflection.html) (if you compile with `-parameters`)
- integrates with Spring but does not require Spring
- supports primitive types
- supports Java 8 Date and Time API
 - if the driver supports it
- does reflection only once per method and caches the meta data for future calls
- interfaces can be mocked or stubbed easily for tests that don't require database access
- no dependencies, Spring is merely an [optional dependency](https://maven.apache.org/guides/introduction/introduction-to-optional-and-excludes-dependencies.html)
- no bytecode parsing
- no runtime bytecode generation or manipulation
- permissive license (MIT)

Not supported
-------------

 - more than one out parameter
 - in out parameters

What problem does this project solve?
-------------------------------------

Calling simple stored procedures in JDBC or JPA is unnecessarily [cumbersome](https://blog.jooq.org/2016/06/08/using-stored-procedures-with-jpa-jdbc-meh-just-use-jooq/). While this may be required in rare cases in common cases this can be solved much easier. None of the common data access layers solve this issue:

- While [spring-jdbc](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/jdbc.html) offers many ways to call a stored procedure all of them require the registration of [SqlParameter](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/jdbc/core/SqlParameter.html) objects and still access database metadata by default. Metadata access has to be [disabled](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/jdbc/core/simple/SimpleJdbcCall.html#withoutProcedureColumnMetaDataAccess--) for every use. The options are:
 - call [JdbcOperations#cal](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/jdbc/core/JdbcOperations.html#call-org.springframework.jdbc.core.CallableStatementCreator-java.util.List-)
 - sublcass [StoredProcedure](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/jdbc/object/StoredProcedure.html)
 - use [SimpleJdbcCall](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/jdbc/core/simple/SimpleJdbcCall.html)
- [Spring Data JPA](https://github.com/spring-projects/spring-data-examples/tree/master/jpa/jpa21) is close.
- [jOOQ](http://www.jooq.org/doc/3.8/manual/sql-execution/stored-procedures/) requires a class for every call, passing a configuration object and calling setters.
- [jDBI](https://github.com/jdbi/jdbi/issues/135) falls back to manual parameter registration for out parameters as well.

While they all have their use case none of them fitted out use case.

Assumptions
-----------

This projects makes a few assumptions about the environment it runs in:

- You have a connection pool.
- You manage transactions either directly or indirectly through JTA or through Spring or a similar way.

Exception Translation
---------------------

Different ways of handling exceptions are offered:

- If the method declares `throws SQLException` no exception translation will happen and the original exception will be propagated.
- If the does not method declare `throws SQLException` exception translation to an `UncheckedSQLException` will happen.
- If the does not method declare `throws SQLException` exception and Spring is present translation will happen using Spring [DataAccessException](http://docs.spring.io/autorepo/docs/spring/current/spring-framework-reference/html/dao.html) hierarchy.

Caveats
-------
- no support for `java.util.Date` or `java.util.Calendar`, because JDBC doesn't support it
- no support for `BigInteger`, because JDBC doesn't support it
- no support for `ZonedDateTime`, because JDBC doesn't support it
- uses [@Annotations](http://www.annotatiomania.com)
- requires some knowledge about how the database driver handles calls (see below)

#### H2
- does not support out parameters, use either `@ReturnValue` or no annotation at all

#### Postgres
- pgjdbc 9.4.1210 or later is recommended

#### Mysql
- ref cursors only work if you use neither `@OutParameter` nor `@ReturnValue`

Unsure
------
- Do we need a package name in addition to a schema name?
- Is there a better way to avoid having `@OutParameter` or `@ReturnValue`
- procedures that use result sets instead of out parameter or return values
- Should we support other collections than list?
- Should we support a ValueExtractor without an int
- Out parameter default last
- name of the SPI package
- type annotation for element in ref cursor
- rename `@OutParameter` to `@Procedure`, `@ReturnValue` to `@Function`
- combine `@OutParameter` and `@Function` and remove attirbutes from `@Function`?


