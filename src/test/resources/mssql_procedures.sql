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

