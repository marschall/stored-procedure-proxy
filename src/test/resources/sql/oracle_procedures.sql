CREATE OR REPLACE FUNCTION sales_tax(subtotal real)
RETURN real AS
BEGIN
    RETURN subtotal * 0.06;
END;
/

CREATE OR REPLACE PROCEDURE property_tax(subtotal real, tax OUT real) AS
BEGIN
    tax := subtotal * 0.06;
END;
/
