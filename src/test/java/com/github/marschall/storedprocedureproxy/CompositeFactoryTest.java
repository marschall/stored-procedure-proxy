package com.github.marschall.storedprocedureproxy;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;

import org.junit.Test;

public class CompositeFactoryTest {

  @Test
  public void testToString() {
    CallResourceFactory factory1 = new ToStringResourceFactory("factory1");
    CallResourceFactory factory2 = new ToStringResourceFactory("factory2");
    CallResourceFactory composite = new CompositeFactory(new CallResourceFactory[] {factory1, factory2});

    assertEquals("CompositeFactory[factory1, factory2]", composite.toString());
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
