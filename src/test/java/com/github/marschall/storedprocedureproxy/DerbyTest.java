package com.github.marschall.storedprocedureproxy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.condition.JRE.JAVA_8;

import java.math.BigDecimal;

import org.junit.jupiter.api.condition.DisabledOnJre;
import org.springframework.test.context.ContextConfiguration;

import com.github.marschall.storedprocedureproxy.ProcedureCallerFactory.ParameterRegistration;
import com.github.marschall.storedprocedureproxy.configuration.DerbyConfiguration;
import com.github.marschall.storedprocedureproxy.procedures.DerbyProcedures;

@DisabledOnJre(JAVA_8)
@ContextConfiguration(classes = DerbyConfiguration.class)
public class DerbyTest extends AbstractDataSourceTest {

  private DerbyProcedures functions(ParameterRegistration parameterRegistration) {
    return ProcedureCallerFactory.of(DerbyProcedures.class, this.getDataSource())
            .withParameterRegistration(parameterRegistration)
            .withNamespace()
            .build();
  }

  @IndexedParametersRegistrationTest
  public void outParameter(ParameterRegistration parameterRegistration) {
    assertThat(this.functions(parameterRegistration).calculateRevenueByMonth(9, 2016), comparesEqualTo(new BigDecimal(201609)));
  }

  @IndexedParametersRegistrationTest
  public void inOutParameter(ParameterRegistration parameterRegistration) {
    assertThat(this.functions(parameterRegistration).raisePrice(new BigDecimal("10.1")), comparesEqualTo(new BigDecimal("20.2")));
  }

  @IndexedParametersRegistrationTest
  public void returnValue(ParameterRegistration parameterRegistration) {
    assertEquals(0.01d, 6.0d, this.functions(parameterRegistration).salesTax(100.0d));
  }

}
