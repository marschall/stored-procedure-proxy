package com.github.marschall.storedprocedureproxy;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.SQLExceptionTranslator;

class SpringSQLExceptionAdapterTest {

  /**
   * Regression test for <a href="https://github.com/marschall/stored-procedure-proxy/issues/71"> #71</a>
   */
  @Test
  void translateReturnsNull() {
    SQLExceptionTranslator translator = mock(SQLExceptionTranslator.class);
    when(translator.translate(anyString(), anyString(), any(SQLException.class))).thenReturn(null);

    SpringSQLExceptionAdapter adapter = new SpringSQLExceptionAdapter(translator);

    DataAccessException translated = adapter.translate("simple_function", "{call simple_function()}", new SQLException());
    assertNotNull(translated);
  }

}
