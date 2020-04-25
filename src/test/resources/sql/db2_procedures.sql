CREATE OR REPLACE FUNCTION sales_tax(subtotal real)
  RETURNS real
  LANGUAGE SQL
  DETERMINISTIC
  BEGIN
    RETURN subtotal * 0.06;
  END
/

CREATE OR REPLACE PROCEDURE property_tax(IN subtotal real, OUT tax real)
  LANGUAGE SQL
  DETERMINISTIC
  BEGIN
    SET tax = subtotal * 0.06;
  END
/

