CREATE OR REPLACE FUNCTION sales_tax(subtotal real)
RETURN real AS
BEGIN
    RETURN subtotal * 0.06;
END;
/

CREATE OR REPLACE PROCEDURE property_tax(subtotal IN real, tax OUT real) AS
BEGIN
    tax := subtotal * 0.06;
END;
/

CREATE OR REPLACE PACKAGE stored_procedure_proxy AS
   TYPE TEST_IDS IS VARRAY(1000) OF NUMBER(10);
   FUNCTION negate_function (b BOOLEAN) 
      RETURN BOOLEAN;
   PROCEDURE negate_procedure(b IN OUT BOOLEAN);
   PROCEDURE array_sum(ids IN TEST_IDS, sum_result OUT NUMBER);
   PROCEDURE return_array(array_result OUT TEST_IDS);
   PROCEDURE return_refcursor(ids OUT SYS_REFCURSOR);
END stored_procedure_proxy; 
/

CREATE OR REPLACE PACKAGE BODY stored_procedure_proxy AS
   FUNCTION negate_function (b BOOLEAN) 
      RETURN BOOLEAN AS
   BEGIN
       RETURN NOT b;
   END;

   PROCEDURE negate_procedure(b IN OUT BOOLEAN) AS
   BEGIN
       b := NOT b;
   END;

   PROCEDURE array_sum(ids IN TEST_IDS, sum_result OUT NUMBER) AS
   BEGIN
       SELECT SUM(COLUMN_VALUE)
       INTO sum_result
       FROM TABLE(ids);
   END;

   PROCEDURE return_array(array_result OUT TEST_IDS) AS
   BEGIN
       array_result := TEST_IDS(1, 2, 3);
   END;

   PROCEDURE return_refcursor(ids OUT SYS_REFCURSOR) AS
   BEGIN
       OPEN ids FOR
           SELECT level id
           FROM dual
           CONNECT BY level <= 3;
   END;
END stored_procedure_proxy; 
/

