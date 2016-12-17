-- https://www.postgresql.org/docs/9.5/static/plpgsql-declarations.html

CREATE OR REPLACE FUNCTION sales_tax(subtotal real)
RETURNS real AS $$
BEGIN
    RETURN subtotal * 0.06;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION property_tax(subtotal real, OUT tax real) AS $$
BEGIN
    tax := subtotal * 0.06;
END;
$$ LANGUAGE plpgsql;


-- https://www.postgresql.org/docs/9.5/static/plpgsql-porting.html

CREATE OR REPLACE FUNCTION cs_fmt_browser_version(v_name varchar,
                                                  v_version varchar)
RETURNS varchar AS $$
BEGIN
    IF v_version IS NULL THEN
        RETURN v_name;
    END IF;
    RETURN v_name || '/' || v_version;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION raise_exception()
RETURNS void AS $$
BEGIN
    RAISE SQLSTATE '22000';
END;
$$ LANGUAGE plpgsql;

-- https://www.postgresql.org/docs/9.5/static/plpgsql-cursors.html

CREATE OR REPLACE FUNCTION simple_ref_cursor() RETURNS refcursor AS $$
DECLARE
    ref refcursor;
BEGIN
    OPEN ref FOR SELECT 'hello' UNION ALL SELECT 'postgres';
    RETURN ref;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION simple_ref_cursor_out(OUT o_strings refcursor) AS $$
BEGIN
    OPEN o_strings FOR SELECT 'hello' UNION ALL SELECT 'postgres';
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION sample_array_argument(input_values int[])
RETURNS varchar AS $$
BEGIN
    return array_to_string(input_values, ', ');
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION array_return_value()
RETURNS int[] AS $$
BEGIN
    return ARRAY [1,2,3,4];
END;
$$ LANGUAGE plpgsql;

