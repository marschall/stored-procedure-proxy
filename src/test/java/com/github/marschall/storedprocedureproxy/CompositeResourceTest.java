package com.github.marschall.storedprocedureproxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.sql.SQLException;

import org.junit.Test;

public class CompositeResourceTest {

  @Test
  public void multipleExceptions() {
    CallResource resource1 = new ThrowingResource("message1");
    CallResource resource2 = new ThrowingResource("message2");
    CallResource composite = new CompositeResource(new CallResource[]{resource1, resource2});

    try {
      composite.close();
      fail("should throw");
    } catch (SQLException e) {
      assertEquals("message1", e.getMessage());
      Throwable[] suppressed = e.getSuppressed();
      assertNotNull(suppressed);
      assertEquals(1, suppressed.length);
      assertEquals("message2", suppressed[0].getMessage());
    }
  }

  @Test
  public void lastException() {
    CallResource resource1 = new NonThrowingResource();
    CallResource resource2 = new ThrowingResource("message2");
    CallResource composite = new CompositeResource(new CallResource[]{resource1, resource2});

    try {
      composite.close();
      fail("should throw");
    } catch (SQLException e) {
      assertEquals("message2", e.getMessage());
      Throwable[] suppressed = e.getSuppressed();
      assertNotNull(suppressed);
      assertEquals(0, suppressed.length);
    }
  }

  @Test
  public void firstException() {
    CallResource resource1 = new ThrowingResource("message1");
    CallResource resource2 = new NonThrowingResource();
    CallResource composite = new CompositeResource(new CallResource[]{resource1, resource2});

    try {
      composite.close();
      fail("should throw");
    } catch (SQLException e) {
      assertEquals("message1", e.getMessage());
      Throwable[] suppressed = e.getSuppressed();
      assertNotNull(suppressed);
      assertEquals(0, suppressed.length);
    }
  }

  @Test
  public void noException() throws SQLException {
    CallResource resource1 = new NonThrowingResource();
    CallResource resource2 = new NonThrowingResource();
    CallResource composite = new CompositeResource(new CallResource[]{resource1, resource2});

    composite.close();
  }

  @Test
  public void testToString() throws SQLException {
    CallResource resource1 = new ToStringResource("resource1");
    CallResource resource2 = new ToStringResource("resource2");

    try (CallResource composite = new CompositeResource(new CallResource[]{resource1, resource2})) {
      assertEquals("CompositeResource[resource1, resource2]", composite.toString());
    }
  }

  static final class NonThrowingResource implements CallResource {


    @Override
    public boolean hasResourceAt(int index) {
      throw new IllegalStateException("should not be called");
    }

    @Override
    public Object resourceAt(int index) {
      throw new IllegalStateException("should not be called");
    }

    @Override
    public void close() throws SQLException {
      // nothing
    }

  }

  static final class ThrowingResource implements CallResource {

    private final String message;

    ThrowingResource(String message) {
      this.message = message;
    }

    @Override
    public boolean hasResourceAt(int index) {
      throw new IllegalStateException("should not be called");
    }

    @Override
    public Object resourceAt(int index) {
      throw new IllegalStateException("should not be called");
    }

    @Override
    public void close() throws SQLException {
      throw new SQLException(this.message);
    }

  }

  static final class ToStringResource implements CallResource {

    private final String s;

    ToStringResource(String s) {
      this.s = s;
    }

    @Override
    public boolean hasResourceAt(int index) {
      throw new IllegalStateException("should not be called");
    }

    @Override
    public Object resourceAt(int index) {
      throw new IllegalStateException("should not be called");
    }

    @Override
    public void close() throws SQLException {
      // ignore
    }

    @Override
    public String toString() {
      return this.s;
    }

  }

}
