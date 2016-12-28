CREATE PROCEDURE plus1inout
   @arg INT,
   @res INT OUTPUT
AS
BEGIN
   SET @res = arg + 1
END;

CREATE FUNCTION plus1inret(@arg int)
RETURNS int
AS
BEGIN
    DECLARE @ret int;
    @ret = @arg + 1;
    RETURN @ret;
END;
