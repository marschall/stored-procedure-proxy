Stored Procedure Proxy
======================

Proxies through which to call stored procedures.

- works with Oracle packages
- works with Oracle functions
- avoids JDBC metadata
-- works if the database user is not the schema owner
-- works if there are hundreds of visible schemas
- names for schemas, procedures and parameters can be supplied explicitly or derived if you have a naming convention
-- supports binding by parameter names
-- parameter names can be read from [source](https://docs.oracle.com/javase/tutorial/reflect/member/methodparameterreflection.html) (if you compile with `-parameters`)
- integrates with Spring but does not require Spring

Not supported

 - more than one out parameter
 - in out parameters

This project assumes you have a connection pool.
This project assumes you manage transactions either directly or indirectly through JTA or through Spring or a similar way.

Exception Translation
---------------------
- if the method declares `throws SQLException` no exception translation will happen
- if the does not method declare `throws SQLException` exception translation to an `UncheckedSQLException` will happen
- if the does not method declare `throws SQLException` exception and Spring is present translation will happen using Spring


https://blog.jooq.org/tag/stored-procedures/

