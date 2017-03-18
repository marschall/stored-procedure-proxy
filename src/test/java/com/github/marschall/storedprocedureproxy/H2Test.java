package com.github.marschall.storedprocedureproxy;

import static com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration.INDEX_AND_TYPE;
import static com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration.INDEX_ONLY;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.test.context.ContextConfiguration;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration;
import com.github.marschall.storedprocedureproxy.configuration.H2Configuration;
import com.github.marschall.storedprocedureproxy.procedures.H2Procedures;
import com.github.marschall.storedprocedureproxy.procedures.H2Procedures.IdName;
import com.github.marschall.storedprocedureproxy.spi.NamingStrategy;

@RunWith(Parameterized.class)
@ContextConfiguration(classes = H2Configuration.class)
public class H2Test extends AbstractDataSourceTest {

  private H2Procedures procedures;

  private ParameterRegistration parameterRegistration;

  public H2Test(ParameterRegistration parameterRegistration) {
    this.parameterRegistration = parameterRegistration;
  }

  @Before
  public void setUp() {
    this.procedures = ProcedureCallerFactory.of(H2Procedures.class, this.getDataSource())
            .withProcedureNamingStrategy(NamingStrategy.snakeCase().thenUpperCase())
            .withParameterRegistration(this.parameterRegistration)
            .build();
  }

  @Parameters
  public static Collection<Object[]> parameters() {
    return Arrays.asList(
            new Object[] {INDEX_ONLY},
            new Object[] {INDEX_AND_TYPE}
    );
  }

  @Test
  public void callScalarFunction() {
    String input = "test";
    assertEquals("pre" + input + "post", procedures.stringProcedure(input));
  }

  @Test
  public void callVoidProcedure() {
    procedures.voidProcedure("test");
  }

  @Test
  public void noArgProcedure() {
    assertEquals("output", procedures.noArgProcedure());
  }

  @Test
  public void reverseObjectArray() {
    Object[] input = new Object[] {1, "null", 1.1d};
    Object[] expected = new Object[] {1.1d, "null", 1};
    assertArrayEquals(expected, procedures.reverseObjectArray(input));
  }

  @Test
  public void reverseIntegerArray() {
    Integer[] input = new Integer[] {11, 2, 15};
    Integer[] expected = new Integer[] {15, 2, 11};
    assertArrayEquals(expected, procedures.reverseIntegerArray(input));
  }

  @Test
  public void returnObjectArray() {
    Object[] expected = new Object[] {1, "string"};
    assertArrayEquals(expected, procedures.returnObjectArray());
  }

  @Test
  public void returnIntegerArray() {
    Integer[] expected = new Integer[] {4, 1, 7};
    assertArrayEquals(expected, procedures.returnIntegerArray());
  }

  @Test
  public void simpleResultSetNumbered() {
    List<IdName> names = procedures.simpleResultSet((rs, i) -> {
      long id = rs.getLong("ID");
      String name = rs.getString("NAME");
      return new IdName(id, i + "-" + name);
    });

    assertThat(names, hasSize(2));

    IdName name = names.get(0);
    assertEquals(0L, name.getId());
    assertEquals("0-Hello", name.getName());

    name = names.get(1);
    assertEquals(1L, name.getId());
    assertEquals("1-World", name.getName());
  }

  @Test
  public void simpleResultSet() {
    List<IdName> names = procedures.simpleResultSet((ValueExtractor<IdName>) rs -> {
      long id = rs.getLong("ID");
      String name = rs.getString("NAME");
      return new IdName(id, name);
    });

    assertThat(names, hasSize(2));

    IdName name = names.get(0);
    assertEquals(0L, name.getId());
    assertEquals("Hello", name.getName());

    name = names.get(1);
    assertEquals(1L, name.getId());
    assertEquals("World", name.getName());
  }

  @Test
  @Ignore("feature not ready")
  public void simpleResultSetFunction() {
    List<IdName> names = procedures.simpleResultSet((Function<ResultSet, IdName>) rs -> {
      long id;
      String name;
      try {
        id = rs.getLong("ID");
        name = rs.getString("NAME");
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
      return new IdName(id, name);
    });

    assertThat(names, hasSize(2));

    IdName name = names.get(0);
    assertEquals(0L, name.getId());
    assertEquals("Hello", name.getName());

    name = names.get(1);
    assertEquals(1L, name.getId());
    assertEquals("World", name.getName());
  }

  @Test
  @Ignore("#23")
  public void simpleResultSetWithDefaultMethod() {
    List<IdName> names = procedures.simpleResultSet();

    assertThat(names, hasSize(2));

    IdName name = names.get(0);
    assertEquals(0L, name.getId());
    assertEquals("Hello", name.getName());

    name = names.get(1);
    assertEquals(1L, name.getId());
    assertEquals("World", name.getName());
  }

}
