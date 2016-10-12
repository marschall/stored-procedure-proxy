CREATE procedure plus1inout (IN arg int, OUT res int)
BEGIN ATOMIC
  set res = arg + 1;
END
/;

CREATE FUNCTION an_hour_before (t TIMESTAMP)
   RETURNS TIMESTAMP
   RETURN t - 1 HOUR;
/;

