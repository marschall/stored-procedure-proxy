Stored Procedure Proxy
======================

Proxies through which to call stored procedures.

- works with Oracle packages
- works with functions as well as procedures with out parameters
- avoids JDBC metadata
 - works if the database user is not the schema owner
 - works if there are hundreds of visible schemas
- names for schemas, procedures and parameters can be supplied explicitly or derived if you have a naming convention
 - supports binding by parameter names
 - parameter names can be read from [source](https://docs.oracle.com/javase/tutorial/reflect/member/methodparameterreflection.html) (if you compile with `-parameters`)
- integrates with Spring but does not require Spring
- supports primitive types
- supports Java 8 Date and Time API
 - if the driver supports it

Not supported

 - more than one out parameter
 - in out parameters

What problem does this project solve
------------------------------------

Calling simple stored procedures in JDBC or JPA is unnecessarily [cumbersome](https://blog.jooq.org/2016/06/08/using-stored-procedures-with-jpa-jdbc-meh-just-use-jooq/). While this may be required in rare cases in common cases this can be solved much easier. None of the common data access layers solve this issue:

- While [spring-jdbc](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/jdbc.html) offers many ways to call a stored procedure all of them require the registration of [SqlParameter](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/jdbc/core/SqlParameter.html) objects and still access database metadata by default. Metadata access has to be disabled for every use. The options are:
-- call [JdbcOperations#cal](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/jdbc/core/JdbcOperations.html#call-org.springframework.jdbc.core.CallableStatementCreator-java.util.List-)
-- sublcass [StoredProcedure](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/jdbc/object/StoredProcedure.html)
-- use [SimpleJdbcCall](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/jdbc/core/simple/SimpleJdbcCall.html)
- [Spring Data JPA](https://github.com/spring-projects/spring-data-examples/tree/master/jpa/jpa21) is hardly in improvement over JPA at all.
- [jDBI](https://github.com/jdbi/jdbi/issues/135) falls back to manual parameter registration for out parameters as well.
- [jOOQ](http://www.jooq.org/doc/3.2/manual/sql-execution/stored-procedures/) requires a class for every call, passing a configuration object and calling setters.

While they all have their use case none of them fitted out use case.

Assumptions
-----------
- You have a connection pool.
- You manage transactions either directly or indirectly through JTA or through Spring or a similar way.

Exception Translation
---------------------
- if the method declares `throws SQLException` no exception translation will happen
- if the does not method declare `throws SQLException` exception translation to an `UncheckedSQLException` will happen
- if the does not method declare `throws SQLException` exception and Spring is present translation will happen using Spring


https://blog.jooq.org/tag/stored-procedures/

Caveats
-------
- no support for `java.util.Date` or `java.util.Calendar`, because JDBC doesn't support it
- no support for `BigInteger`, because JDBC doesn't support it
- all H2 procedures must be annotated with `@ReturnValue` because H2 does not support OUT parameters
- uses [@Annotations](http://www.annotatiomania.com)


