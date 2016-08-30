
DROP FUNCTION IF EXISTS hello_function;
CREATE FUNCTION hello_function (s CHAR(20))
RETURNS CHAR(50) DETERMINISTIC
RETURN CONCAT('Hello, ',s,'!');

-- DELIMITER //
DROP PROCEDURE IF EXISTS hello_procedure;
CREATE PROCEDURE hello_procedure (IN s CHAR(20), OUT result VARCHAR(100))
-- BEGIN
SET result = CONCAT('Hello, ',s,'!');
-- END
-- END //

DROP PROCEDURE IF EXISTS fake_refcursor;
CREATE PROCEDURE fake_refcursor ()
-- BEGIN
SELECT 'hello' UNION ALL SELECT 'mysql';

