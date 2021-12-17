package com.github.marschall.storedprocedureproxy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.springframework.test.context.ContextConfiguration;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration;
import com.github.marschall.storedprocedureproxy.configuration.H2Configuration;
import com.github.marschall.storedprocedureproxy.procedures.H2Procedures;
import com.github.marschall.storedprocedureproxy.procedures.H2Procedures.IdName;
import com.github.marschall.storedprocedureproxy.spi.NamingStrategy;

@ContextConfiguration(classes = H2Configuration.class)
public class H2IT extends AbstractDataSourceTest {

  private H2Procedures procedures(ParameterRegistration parameterRegistration) {
    return ProcedureCallerFactory.of(H2Procedures.class, this.getDataSource())
            .withProcedureNamingStrategy(NamingStrategy.snakeCase().thenUpperCase())
            .withParameterRegistration(parameterRegistration)
            .build();
  }

  @IndexedParametersRegistrationTest
  public void simpleResultSetWithDefaultMethod(ParameterRegistration parameterRegistration) {
    if (isJava9OrLater()) {
      List<IdName> names = this.procedures(parameterRegistration)
              .simpleResultSet();
      assertThat(names, hasSize(2));
      IdName name = names.get(0);
      assertEquals(0L, name.getId());
      assertEquals("Hello", name.getName());
      name = names.get(1);
      assertEquals(1L, name.getId());
      assertEquals("World", name.getName());
    } else {
      assertThrows(IllegalStateException.class, () -> {
        this.procedures(parameterRegistration)
          .simpleResultSet();
      });
    }
  }

  private static boolean isJava9OrLater() {
    try {
      Class.forName("java.lang.Runtime$Version");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

}
