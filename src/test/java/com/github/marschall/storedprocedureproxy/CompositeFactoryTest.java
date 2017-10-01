package com.github.marschall.storedprocedureproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

public class CompositeFactoryTest {

  @Test
  public void exceptionDuringCreation() throws SQLException {
    // given
    String exceptionMessage = "Premium Bier";
    String supressedMessage = "Club-Mate";
    Connection connection = mock(Connection.class);
    CallResource throwOnCloseResource = mock(CallResource.class);
    CallResource noThrowResource = mock(CallResource.class);
    doThrow(new SQLException(supressedMessage)).when(throwOnCloseResource).close();;

    CallResourceFactory factory = new CompositeFactory(new CallResourceFactory[] {
        new DelegatingResourceFactory(throwOnCloseResource),
        new DelegatingResourceFactory(noThrowResource),
        new ThrowingResourceFactory(exceptionMessage)
    });

    // when
    SQLException e = assertThrows(SQLException.class, () -> factory.createResource(connection, new Object[0]));
    assertEquals(exceptionMessage, e.getMessage());
    Throwable[] suppressed = e.getSuppressed();
    assertNotNull(suppressed);
    assertEquals(1, suppressed.length);
    assertEquals(supressedMessage, suppressed[0].getMessage());

    // then
    verify(throwOnCloseResource, times(1)).close();
    verify(noThrowResource, times(1)).close();
  }

  @Test
  public void testToString() {
    CallResourceFactory factory1 = new ToStringResourceFactory("factory1");
    CallResourceFactory factory2 = new ToStringResourceFactory("factory2");
    CallResourceFactory composite = new CompositeFactory(new CallResourceFactory[] {factory1, factory2});

    assertEquals("CompositeFactory[factory1, factory2]", composite.toString());
  }

  static final class DelegatingResourceFactory implements CallResourceFactory {

    private final CallResource callResource;

    DelegatingResourceFactory(CallResource callResource) {
      this.callResource = callResource;
    }

    @Override
    public CallResource createResource(Connection connection, Object[] args) {
      return this.callResource;
    }

  }

  static final class ThrowingResourceFactory implements CallResourceFactory {

    private final String message;

    ThrowingResourceFactory(String message) {
      this.message = message;
    }

    @Override
    public CallResource createResource(Connection connection, Object[] args)throws SQLException {
      throw new SQLException(this.message);
    }

  }

  static final class ToStringResourceFactory implements CallResourceFactory {

    private final String s;

    ToStringResourceFactory(String s) {
      this.s = s;
    }

    @Override
    public CallResource createResource(Connection connection, Object[] args) {
      throw new IllegalStateException("should not be called");
    }

    @Override
    public String toString() {
      return this.s;
    }

  }

}
