package com.github.marschall.storedprocedureproxy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Disabled;
import org.springframework.test.context.ContextConfiguration;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration;
import com.github.marschall.storedprocedureproxy.configuration.H2Configuration;
import com.github.marschall.storedprocedureproxy.procedures.H2Procedures;
import com.github.marschall.storedprocedureproxy.procedures.H2Procedures.IdName;
import com.github.marschall.storedprocedureproxy.spi.NamingStrategy;

@ContextConfiguration(classes = H2Configuration.class)
public class H2Test extends AbstractDataSourceTest {

  private H2Procedures procedures(ParameterRegistration parameterRegistration) {
    return ProcedureCallerFactory.of(H2Procedures.class, this.getDataSource())
            .withProcedureNamingStrategy(NamingStrategy.snakeCase().thenUpperCase())
            .withParameterRegistration(parameterRegistration)
            .build();
  }

  @IndexedParametersRegistrationTest
  public void callScalarFunction(ParameterRegistration parameterRegistration) {
    String input = "test";
    assertEquals("pre" + input + "post", this.procedures(parameterRegistration).stringProcedure(input));
  }

  @IndexedParametersRegistrationTest
  public void callVoidProcedure(ParameterRegistration parameterRegistration) {
    this.procedures(parameterRegistration).voidProcedure("test");
  }

  @IndexedParametersRegistrationTest
  public void noArgProcedure(ParameterRegistration parameterRegistration) {
    assertEquals("output", this.procedures(parameterRegistration).noArgProcedure());
  }

  @IndexedParametersRegistrationTest
  public void reverseIntegerArray(ParameterRegistration parameterRegistration) {
    Integer[] input = new Integer[] {11, 2, 15};
    Integer[] expected = new Integer[] {15, 2, 11};
    assertArrayEquals(expected, this.procedures(parameterRegistration).reverseIntegerArray(input));
  }

  @IndexedParametersRegistrationTest
  public void returnIntegerArray(ParameterRegistration parameterRegistration) {
    Integer[] expected = new Integer[] {4, 1, 7};
    assertArrayEquals(expected, this.procedures(parameterRegistration).returnIntegerArray());
  }

  @IndexedParametersRegistrationTest
  public void simpleResultSetNumbered(ParameterRegistration parameterRegistration) {
    List<IdName> names = this.procedures(parameterRegistration).simpleResultSet((rs, i) -> {
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

  @IndexedParametersRegistrationTest
  public void simpleResultSet(ParameterRegistration parameterRegistration) {
    List<IdName> names = this.procedures(parameterRegistration).simpleResultSet((ValueExtractor<IdName>) rs -> {
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

  @IndexedParametersRegistrationTest
  @Disabled("feature not ready")
  public void simpleResultSetFunction(ParameterRegistration parameterRegistration) {
    List<IdName> names = this.procedures(parameterRegistration).simpleResultSet((Function<ResultSet, IdName>) rs -> {
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

}
