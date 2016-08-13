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

import javax.sql.DataSource;

import com.github.marschall.springjdbccall.annotations.ParameterName;
import com.github.marschall.springjdbccall.annotations.ParameterType;
import com.github.marschall.springjdbccall.annotations.ProcedureName;
import com.github.marschall.springjdbccall.annotations.ReturnValue;
import com.github.marschall.springjdbccall.annotations.SchemaName;
import com.github.marschall.springjdbccall.spi.NamingStrategy;

public final class ProcedureCallerFactory<T> {

  private static final boolean HAS_SPRING;

  private static final IncorrectResultSizeExceptionGenerator INCORRECT_RESULT_SIZE_EXCEPTION_GENERATOR;

  static {
    boolean hasSpring;
    IncorrectResultSizeExceptionGenerator incorrectResultSizeExceptionGenerator;
    try {
      Class.forName("org.springframework.jdbc.support.SQLExceptionTranslator", false, ProcedureCallerFactory.class.getClassLoader());
      hasSpring = true;
      incorrectResultSizeExceptionGenerator = new DefaultIncorrectResultSizeExceptionGenerator();
    } catch (ClassNotFoundException e) {
      hasSpring = false;
      incorrectResultSizeExceptionGenerator = new SpringIncorrectResultSizeExceptionGenerator();
    }
    HAS_SPRING = hasSpring;
    INCORRECT_RESULT_SIZE_EXCEPTION_GENERATOR = incorrectResultSizeExceptionGenerator;
  }

  private final Class<T> inferfaceDeclaration;

  private final DataSource dataSource;

  private NamingStrategy parameterNamingStrategy;

  private NamingStrategy procedureNamingStrategy;

  private NamingStrategy schemaNamingStrategy;

  private boolean hasSchemaName;

  private ParameterRegistration parameterRegistration;

  private SQLExceptionAdapter exceptionAdapter;

  private ProcedureCallerFactory(Class<T> inferfaceDeclaration, DataSource dataSource) {
    this.inferfaceDeclaration = inferfaceDeclaration;
    this.dataSource = dataSource;
    this.parameterNamingStrategy = NamingStrategy.IDENTITY;
    this.procedureNamingStrategy = NamingStrategy.IDENTITY;
    this.schemaNamingStrategy = NamingStrategy.IDENTITY;
    this.hasSchemaName = false;
    this.parameterRegistration = ParameterRegistration.INDEX_ONLY;
    this.exceptionAdapter = getDefaultExceptionAdapter(dataSource);
  }

  private static SQLExceptionAdapter getDefaultExceptionAdapter(DataSource dataSource) {
    if (HAS_SPRING) {
      return new SpringSQLExceptionAdapter(dataSource);
    } else {
      return UncheckedSQLExceptionAdapter.INSTANCE;
    }
  }


  public static <T> ProcedureCallerFactory<T> of(Class<T> inferfaceDeclaration, DataSource dataSource) {
    Objects.requireNonNull(inferfaceDeclaration);
    Objects.requireNonNull(dataSource);
    return new ProcedureCallerFactory<>(inferfaceDeclaration, dataSource);
  }

  public static <T> T build(Class<T> inferfaceDeclaration, DataSource dataSource) {
    return of(inferfaceDeclaration, dataSource).build();
  }
  public ProcedureCallerFactory<T> withParameterNamingStrategy(NamingStrategy parameterNamingStrategy) {
    this.parameterNamingStrategy = parameterNamingStrategy;
    return this;
  }

  public ProcedureCallerFactory<T> withProcedureNamingStrategy(NamingStrategy procedureNamingStrategy) {
    this.procedureNamingStrategy = procedureNamingStrategy;
    return this;
  }

  public ProcedureCallerFactory<T> withSchemaNamingStrategy(NamingStrategy schemaNamingStrategy) {
    this.schemaNamingStrategy = schemaNamingStrategy;
    return this;
  }

  public ProcedureCallerFactory<T> withSchemaName() {
    this.hasSchemaName = true;
    return this;
  }

  public ProcedureCallerFactory<T> withParameterRegistration(ParameterRegistration parameterRegistration) {
    this.parameterRegistration = parameterRegistration;
    return this;
  }

  public T build() {
    ProcedureCaller caller = new ProcedureCaller(this.dataSource, this.parameterNamingStrategy,
            this.procedureNamingStrategy, this.schemaNamingStrategy, this.hasSchemaName,
            this.parameterRegistration, this.exceptionAdapter);
    // REVIEW correct class loader
    Object proxy = Proxy.newProxyInstance(this.inferfaceDeclaration.getClassLoader(),
            new Class<?>[]{this.inferfaceDeclaration}, caller);
    return this.inferfaceDeclaration.cast(proxy);
  }

  static RuntimeException newIncorrectResultSizeException(int expectedSize, int actualSize) {
    return INCORRECT_RESULT_SIZE_EXCEPTION_GENERATOR.newIncorrectResultSizeException(expectedSize, actualSize);
  }

  public enum ParameterRegistration {
    INDEX_ONLY,
    NAME_ONLY,
    INDEX_AND_TYPE,
    NAME_AND_TYPE;
  }

  static final class ProcedureCaller implements InvocationHandler {

    private final DataSource dataSource;

    private final NamingStrategy parameterNamingStrategy;

    private final NamingStrategy procedureNamingStrategy;

    private final NamingStrategy schemaNamingStrategy;

    private final boolean hasSchemaName;

    private final ParameterRegistration parameterRegistration;

    private final SQLExceptionAdapter exceptionAdapter;

    ProcedureCaller(DataSource dataSource,
            NamingStrategy parameterNamingStrategy,
            NamingStrategy procedureNamingStrategy,
            NamingStrategy schemaNamingStrategy, boolean hasSchemaName,
            ParameterRegistration parameterRegistration,
            SQLExceptionAdapter exceptionAdapter) {
      this.dataSource = dataSource;
      this.parameterNamingStrategy = parameterNamingStrategy;
      this.procedureNamingStrategy = procedureNamingStrategy;
      this.schemaNamingStrategy = schemaNamingStrategy;
      this.hasSchemaName = hasSchemaName;
      this.parameterRegistration = parameterRegistration;
      this.exceptionAdapter = exceptionAdapter;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      Class<?> returnType = method.getReturnType();
      Object returnValue;
      String callString = null;
      try (Connection connection = this.dataSource.getConnection()) {
        callString = buildCallString(method, args);
        try (CallableStatement statement = this.prepareCall(connection, callString)) {
          this.bindParameters(statement, method, args);
          returnValue = this.execute(statement, returnType);
        }
      } catch (SQLException e) {
        throw translate(e, method, callString);
      }
      return returnType.cast(returnValue);
    }

    private Exception translate(SQLException exception, Method method, String callString) {
      if (wantsExceptionTranslation(method)) {
        return this.exceptionAdapter.translate(null, callString, exception);
      } else {
        return exception;
      }
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

    private Object executeScalarMethod(CallableStatement statement) throws SQLException {
      int count = 0;
      Object last = null;
      try (ResultSet rs = statement.executeQuery()) {
        while (rs.next()) {
          last = rs.getObject(1);
          count += 1;
        }
      }
      if (count != 1) {
        ProcedureCallerFactory.newIncorrectResultSizeException(1, count);
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
      // don't check count H2 just returns NULL
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

    private CallableStatement prepareCall(Connection connection, String callString) throws SQLException {
      return connection.prepareCall(callString);
    }

    private String buildCallString(Method method, Object[] args) {
      String procedureName = this.extractProcedureName(method);
      boolean hasReturnValue = hasReturnValue(method);
//      if (hasReturnValue) {
//        if (this.hasSchemaName) {
//          return buildQualifiedFunctionCallString(procedureName, this.extractSchemaName(method), args.length);
//        } else {
//          return buildSimpleFunctionCallString(procedureName, args.length);
//        }
//      } else {
        if (this.hasSchemaName) {
          return buildQualifiedProcedureCallString(procedureName, this.extractSchemaName(method), args.length);
        } else {
          return buildSimpleProcudureCallString(procedureName, args.length);
        }
//      }
    }

    static String buildSimpleProcudureCallString(String functionName, int parameterCount) {
      // {call RAISE_PRICE(?,?,?)}
      StringBuilder builder = new StringBuilder(
              6 // {call
              + functionName.length()
              + 1 // (
              + Math.max(parameterCount * 2 - 1, 0) // ?,?
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

    static String buildQualifiedProcedureCallString(String schemaName, String functionName, int parameterCount) {
      // {call RAISE_PRICE(?,?,?)}
      StringBuilder builder = new StringBuilder(
              6 // {call
              + schemaName.length()
              + 1 // .
              + functionName.length()
              + 1 // (
              + Math.max(parameterCount * 2 - 1, 0) // ?,?
              + 2 // )}
              );
      builder.append("{call ");
      builder.append(schemaName);
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

    static String buildSimpleFunctionCallString(String functionName, int parameterCount) {
      // { ? = call RAISE_PRICE(?,?,?)}
      StringBuilder builder = new StringBuilder(
              11 // { ? = call
              + functionName.length()
              + 1 // (
              + Math.max(parameterCount * 2 - 1, 0) // ?,?
              + 2 // )}
              );
      builder.append("{ ? = call ");
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

    static String buildQualifiedFunctionCallString(String schemaName, String functionName, int parameterCount) {
      // { ? = call RAISE_PRICE(?,?,?)}
      StringBuilder builder = new StringBuilder(
              11 // { ? = call
              + schemaName.length()
              + 1 // .
              + functionName.length()
              + 1 // (
              + Math.max(parameterCount * 2 - 1, 0) // ?,?
              + 2 // )}
              );
      builder.append("{ ? = call ");
      builder.append(schemaName);
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

    private static boolean hasReturnValue(Method method) {
      return method.getAnnotation(ReturnValue.class) != null;
    }

    private static boolean wantsExceptionTranslation(Method method) {
      for (Class<?> exceptionType : method.getExceptionTypes()) {
        if (exceptionType == SQLException.class) {
          return false;
        }
      }
      return true;
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

    private void bindOutParameterByType(CallableStatement statement, int index) throws SQLException {
      statement.registerOutParameter(index, Types.OTHER);
    }

    private void bindOutParameterByIndexAndType(CallableStatement statement, int index, int type) throws SQLException {
      statement.registerOutParameter(index, type);
    }

    private void bindOutParameterByName(CallableStatement statement, String name) throws SQLException {
      statement.registerOutParameter(name, Types.OTHER);
    }

    private void bindOutParameterByNameAndType(CallableStatement statement, String name, int type) throws SQLException {
      statement.registerOutParameter(name, type);
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
