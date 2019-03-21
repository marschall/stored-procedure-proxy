CREATE OR ALTER  PROCEDURE increment(y INTEGER)
RETURNS( x INTEGER)
AS
  BEGIN
  x = y + 1;
  SUSPEND;
END^

CREATE PROCEDURE factorial(max_value INTEGER)
RETURNS (factorial INTEGER)
AS
  DECLARE VARIABLE temp INTEGER;
  DECLARE VARIABLE row_num INTEGER;
BEGIN
  row_num = 0;
  WHILE (row_num <= max_value) DO BEGIN
    IF (row_num <= 1) THEN
      temp = 1;
   ELSE
     temp = temp * row_num;
   factorial = temp;
   row_num = row_num + 1;
   SUSPEND;
  END
END^
