
DROP FUNCTION IF EXISTS hello_function;
CREATE FUNCTION hello_function (s CHAR(20))
RETURNS CHAR(50) DETERMINISTIC
RETURN CONCAT('Hello, ',s,'!');

DROP PROCEDURE IF EXISTS hello_procedure;
CREATE PROCEDURE hello_procedure (IN s CHAR(20), OUT result VARCHAR(100))
SET result = CONCAT('Hello, ',s,'!');

DROP PROCEDURE IF EXISTS fake_refcursor;
CREATE PROCEDURE fake_refcursor ()
SELECT 'hello' UNION ALL SELECT 'mysql';

