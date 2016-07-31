package com.github.marschall.springjdbccall;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Objects;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcOperations;

import com.github.marschall.springjdbccall.annotations.ParameterName;
import com.github.marschall.springjdbccall.annotations.ParameterType;
import com.github.marschall.springjdbccall.annotations.ProcedureName;
import com.github.marschall.springjdbccall.annotations.SchemaName;
import com.github.marschall.springjdbccall.spi.NamingStrategy;

public final class PackageCallerFactory<T> {

  private final Class<T> inferfaceDeclaration;

  private JdbcOperations jdbcOperations;

  private NamingStrategy parameterNamingStrategy;

  private NamingStrategy procedureNamingStrategy;

  private NamingStrategy schemaNamingStrategy;

  private boolean hasSchemaName;

  private ParameterRegistration parameterRegistration;

  private PackageCallerFactory(Class<T> inferfaceDeclaration, JdbcOperations jdbcOperations) {
    this.inferfaceDeclaration = inferfaceDeclaration;
    this.jdbcOperations = jdbcOperations;
    this.parameterNamingStrategy = NamingStrategy.IDENTITY;
    this.procedureNamingStrategy = NamingStrategy.IDENTITY;
    this.schemaNamingStrategy = NamingStrategy.IDENTITY;
    this.hasSchemaName = false;
    this.parameterRegistration = ParameterRegistration.INDEX_ONLY;
  }


  public static <T> PackageCallerFactory<T> of(Class<T> inferfaceDeclaration, JdbcOperations jdbcTemplate) {
    Objects.requireNonNull(jdbcTemplate);
    Objects.requireNonNull(inferfaceDeclaration);
    return new PackageCallerFactory<>(inferfaceDeclaration, jdbcTemplate);
  }

  public static <T> T build(Class<T> inferfaceDeclaration, JdbcOperations jdbcTemplate) {
    return of(inferfaceDeclaration, jdbcTemplate).build();
  }
  public PackageCallerFactory<T> withParameterNamingStrategy(NamingStrategy parameterNamingStrategy) {
    this.parameterNamingStrategy = parameterNamingStrategy;
    return this;
  }

  public PackageCallerFactory<T> withProcedureNamingStrategy(NamingStrategy procedureNamingStrategy) {
    this.procedureNamingStrategy = procedureNamingStrategy;
    return this;
  }

  public PackageCallerFactory<T> withSchemaNamingStrategy(NamingStrategy schemaNamingStrategy) {
    this.schemaNamingStrategy = schemaNamingStrategy;
    return this;
  }

  public PackageCallerFactory<T> withSchemaName() {
    this.hasSchemaName = true;
    return this;
  }

  public PackageCallerFactory<T> withParameterRegistration(ParameterRegistration parameterRegistration) {
    this.parameterRegistration = parameterRegistration;
    return this;
  }

  public T build() {
    PackageCaller caller = new PackageCaller(this.jdbcOperations, this.parameterNamingStrategy,
            this.procedureNamingStrategy, this.schemaNamingStrategy, this.hasSchemaName,
            this.parameterRegistration);
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

    private final NamingStrategy procedureNamingStrategy;

    private final NamingStrategy schemaNamingStrategy;

    private final boolean hasSchemaName;

    private ParameterRegistration parameterRegistration;

    PackageCaller(JdbcOperations jdbcOperations,
            NamingStrategy parameterNamingStrategy,
            NamingStrategy procedureNamingStrategy,
            NamingStrategy schemaNamingStrategy, boolean hasSchemaName,
            ParameterRegistration parameterRegistration) {
      this.jdbcOperations = jdbcOperations;
      this.parameterNamingStrategy = parameterNamingStrategy;
      this.procedureNamingStrategy = procedureNamingStrategy;
      this.schemaNamingStrategy = schemaNamingStrategy;
      this.hasSchemaName = hasSchemaName;
      this.parameterRegistration = parameterRegistration;
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
        try (CallableStatement statement = this.prepareCall(connection, proxy, method, args)) {
          this.bindParameters(statement, method, args);
          return this.execute(statement, returnType);
        }
      });
      return returnType.cast(returnValue);
    }

    private Object execute(CallableStatement statement, Class<?> returnType) throws SQLException {
      if (returnType == Void.TYPE) {
        return executeVoidMethod(statement);
      } else {
        if (Collection.class.isAssignableFrom(returnType)) {
          // TODO Auto-generated method stub
          throw new IllegalArgumentException("collections not yet implemented");
        }
        return executeScalarMethod(statement);
      }
    }

    private Object executeScalarMethod(CallableStatement statement)
            throws SQLException {
      int count = 0;
      Object last = null;
      try (ResultSet rs = statement.executeQuery()) {
        while (rs.next()) {
          last = rs.getObject(1);
          count += 1;
        }
      }
      if (count != 1) {
        throw new IncorrectResultSizeDataAccessException(1, count);
      }
      return last;
    }

    private Object executeVoidMethod(CallableStatement statement) throws SQLException {
      int count = 0;
      try (ResultSet rs = statement.executeQuery()) {
        while (rs.next()) {
          count += 1;
        }
      }
      if (count != 0) {
        throw new IncorrectResultSizeDataAccessException(0, count);
      }
      return null;
    }

    private void bindParameters(CallableStatement statement, Method method, Object[] args) throws SQLException {
      switch (this.parameterRegistration) {
        case INDEX_ONLY:
          this.bindParametersByIndex(statement, args);
          break;
        case NAME_ONLY:
          this.bindParametersByName(statement, this.extractParameterNames(method), args);
          break;
        case INDEX_AND_TYPE:
          this.bindParametersByIndexAndType(statement, args, this.extractParameterTypes(method));
          break;
        case NAME_AND_TYPE:
          this.bindParametersByNameAndType(statement, this.extractParameterNames(method), args, this.extractParameterTypes(method));
          break;
        default:
          throw new IllegalStateException("unknown parameter registration: " + this.parameterRegistration);
      }
    }

    private String[] extractParameterNames(Method method) {
      Parameter[] parameters = method.getParameters();
      String[] names = new String[parameters.length];
      for (int i = 0; i < parameters.length; i++) {
        Parameter parameter = parameters[i];
        ParameterName annotation = parameter.getAnnotation(ParameterName.class);
        String parameterName;
        if (annotation != null) {
          parameterName = annotation.value();
        } else {
          parameterName = this.parameterNamingStrategy.translateToDatabase(parameter.getName());
        }
        names[i] = parameterName;
      }
      return names;
    }

    private int[] extractParameterTypes(Method method) {
      Parameter[] parameters = method.getParameters();
      int[] types = new int[parameters.length];
      for (int i = 0; i < parameters.length; i++) {
        Parameter parameter = parameters[i];
        ParameterType annotation = parameter.getAnnotation(ParameterType.class);
        int type;
        if (annotation != null) {
          type = annotation.value();
        } else {
          // TODO basic type mapping
          type = Types.OTHER;
        }
        types[i] = type;
      }
      return types;
    }

    private CallableStatement prepareCall(Connection connection, Object proxy, Method method, Object[] args) throws SQLException {
      String callString = buildCallString(method, args);
      return connection.prepareCall(callString);
    }

    private String buildCallString(Method method, Object[] args) {
      String procedureName = this.extractProcedureName(method);
      if (this.hasSchemaName) {
        return buildQualifiedCallString(procedureName, this.extractSchemaName(method), args.length);
      } else {
        return buildSimpleCallString(procedureName, args.length);
      }
    }

    private String extractProcedureName(Method method) {
      ProcedureName procedureName = method.getAnnotation(ProcedureName.class);
      if (procedureName != null) {
        return procedureName.value();
      } else {
        return this.procedureNamingStrategy.translateToDatabase(method.getName());
      }
    }

    private String extractSchemaName(Method method) {
      Class<?> declaringClass = method.getDeclaringClass();
      SchemaName schemaName = declaringClass.getAnnotation(SchemaName.class);
      if (schemaName != null) {
        return schemaName.value();
      } else {
        return this.schemaNamingStrategy.translateToDatabase(declaringClass.getName());
      }
    }

    private void bindParametersByIndex(CallableStatement statement, Object[] args) throws SQLException {
      for (int i = 0; i < args.length; i++) {
        statement.setObject(i + 1, args[i]);
      }
    }

    private void bindParametersByIndexAndType(CallableStatement statement, Object[] args, int[] types) throws SQLException {
      for (int i = 0; i < args.length; i++) {
        statement.setObject(i + 1, args[i], types[i]);
      }
    }

    private void bindParametersByName(CallableStatement statement, String[] names, Object[] args) throws SQLException {
      for (int i = 0; i < args.length; i++) {
        statement.setObject(names[i], args[i]);
      }
    }

    private void bindParametersByNameAndType(CallableStatement statement, String[] names, Object[] args, int[] types) throws SQLException {
      for (int i = 0; i < args.length; i++) {
        statement.setObject(names[i], args[i], types[i]);
      }
    }

  }

}
