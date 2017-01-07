DROP PROCEDURE IF EXISTS plus1inout;

CREATE PROCEDURE plus1inout
    @arg INT,
    @res INT OUTPUT
AS
BEGIN
    SET @res = @arg + 1
END;

DROP FUNCTION IF EXISTS plus1inret;

CREATE FUNCTION plus1inret(@arg int)
RETURNS int
AS
BEGIN
    RETURN @arg + 1
END;

-- DROP PROCEDURE IF EXISTS simpleCursor;
-- 
-- CREATE PROCEDURE simpleCursor
--     @OutputCursor CURSOR VARYING OUTPUT
-- AS
-- BEGIN
--     SET @OutputCursor = CURSOR
--     FORWARD_ONLY STATIC FOR
--     SELECT 'hello' UNION ALL SELECT 'world';
-- 
--     OPEN @OutputCursor;
--     return;
-- END;

DROP PROCEDURE IF EXISTS fakeCursor;

CREATE PROCEDURE fakeCursor
AS
BEGIN
    SELECT 'hello' UNION ALL SELECT 'world'
END;


