

- works with Oracle packages
- works with Oracle functions
- avoids JDBC metadata
-- works if the database user is not the schema owner
-- works if there are hundreds of visible schemas
- supports binding by parameter names
-- parameter names can be read from [source](https://docs.oracle.com/javase/tutorial/reflect/member/methodparameterreflection.html) (if you compile with `-parameters`)
- names for schemas, procedures and parameters can be supplied explicitly or derived if you have a naming convention

