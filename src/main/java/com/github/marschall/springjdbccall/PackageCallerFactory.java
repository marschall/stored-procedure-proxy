package com.github.marschall.springjdbccall;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

import org.springframework.jdbc.core.JdbcOperations;

import com.github.marschall.springjdbccall.spi.NamingStrategy;

public final class PackageCallerFactory<T> {

  private final Class<T> inferfaceDeclaration;
  private final JdbcOperations jdbcOperations;


  private PackageCallerFactory(Class<T> inferfaceDeclaration, JdbcOperations jdbcOperations) {
    this.inferfaceDeclaration = inferfaceDeclaration;
    this.jdbcOperations = jdbcOperations;
  }


  public static <T> PackageCallerFactory<T> of(Class<T> inferfaceDeclaration, JdbcOperations jdbcTemplate) {
    Objects.requireNonNull(jdbcTemplate);
    Objects.requireNonNull(inferfaceDeclaration);
    return new PackageCallerFactory<>(inferfaceDeclaration, jdbcTemplate);
  }

  public static <T> T build(Class<T> inferfaceDeclaration, JdbcOperations jdbcTemplate) {
    return of(inferfaceDeclaration, jdbcTemplate).build();
  }


  public T build() {
    PackageCaller caller = new PackageCaller(this.jdbcOperations);
    // REVIEW correct class loader
    Object proxy = Proxy.newProxyInstance(this.inferfaceDeclaration.getClassLoader(),
            new Class<?>[]{this.inferfaceDeclaration}, caller);
    return this.inferfaceDeclaration.cast(proxy);
  }

  public enum ParameterRegistration {
    INDEX_ONLY,
    NAME_ONLY,
    INDEX_AND_TYPE,
    NAME_AND_TYPE;
  }

  static final class PackageCaller implements InvocationHandler {

    private final JdbcOperations jdbcOperations;

    private final NamingStrategy parameterNamingStrategy;

    PackageCaller(JdbcOperations jdbcOperations) {
      this.jdbcOperations = jdbcOperations;
      this.parameterNamingStrategy = (s) -> s;
    }

    static String buildSimpleCallString(String functionName, int parameterCount) {
      // {call RAISE_PRICE(?,?,?)}
      // {call RAISE_PRICE(?,?,?)}
      StringBuilder builder = new StringBuilder(
              6 // {call
              + functionName.length()
              + 1 // (
              + 2 // )}
              );
      builder.append("{call ");
      builder.append(functionName);
      builder.append('(');
      for (int i = 0; i < parameterCount; i++) {
        if (i != 0) {
          builder.append(',');
        }
        builder.append('?');
      }
      builder.append(")}");
      return builder.toString();

    }

    static String buildQualifiedCallString(String packageName, String functionName, int parameterCount) {
      // {call RAISE_PRICE(?,?,?)}
      StringBuilder builder = new StringBuilder(
              6 // {call
              + packageName.length()
              + 1 // .
              + functionName.length()
              + 1 // (
              + 2 // )}
              );
      builder.append("{call ");
      builder.append(packageName);
      builder.append('.');
      builder.append(functionName);
      builder.append('(');
      for (int i = 0; i < parameterCount; i++) {
        if (i != 0) {
          builder.append(',');
        }
        builder.append('?');
      }
      builder.append(")}");
      return builder.toString();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      Class<?> returnType = method.getReturnType();
      Object returnValue = this.jdbcOperations.execute((Connection connection) -> {
        try (CallableStatement statement = this.prepareCall(proxy, method, args)) {
          this.bindParameter(args);
          return this.execute(statement);
        }
      });
      return returnType.cast(returnValue);
    }

    private Object execute(CallableStatement statement) {
      // TODO Auto-generated method stub
      return null;
    }

    private void bindParameter(Object[] args) {
      // TODO Auto-generated method stub

    }

    private CallableStatement prepareCall(Object proxy, Method method, Object[] args) {
      // TODO Auto-generated method stub
      return null;
    }

    private void registerArgumentsByIndex(CallableStatement statement, Object[] args) throws SQLException {
      for (int i = 0; i < args.length; i++) {
        statement.setObject(i, args[i]);
      }
    }

    private void registerArgumentsByIndexAndType(CallableStatement statement, int[] types, Object[] args) throws SQLException {
      for (int i = 0; i < args.length; i++) {
        statement.setObject(i, args[i], types[i]);
      }
    }

    private void registerArgumentsByName(CallableStatement statement, String[] names, Object[] args) throws SQLException {
      for (int i = 0; i < args.length; i++) {
        statement.setObject(names[i], args[i]);
      }
    }

    private boolean hasReturnValue(Method method) {
      return method.getReturnType() == Void.TYPE;
    }

  }

}
