package com.github.marschall.springjdbccall;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import java.util.Collection;
import java.util.Objects;

import javax.sql.DataSource;

import com.github.marschall.springjdbccall.annotations.OutParameter;
import com.github.marschall.springjdbccall.annotations.ParameterName;
import com.github.marschall.springjdbccall.annotations.ParameterType;
import com.github.marschall.springjdbccall.annotations.ProcedureName;
import com.github.marschall.springjdbccall.annotations.ReturnValue;
import com.github.marschall.springjdbccall.annotations.SchemaName;
import com.github.marschall.springjdbccall.spi.NamingStrategy;
import com.github.marschall.springjdbccall.spi.TypeMapper;

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

  private TypeMapper typeMapper;

  private ProcedureCallerFactory(Class<T> inferfaceDeclaration, DataSource dataSource) {
    this.inferfaceDeclaration = inferfaceDeclaration;
    this.dataSource = dataSource;
    this.parameterNamingStrategy = NamingStrategy.IDENTITY;
    this.procedureNamingStrategy = NamingStrategy.IDENTITY;
    this.schemaNamingStrategy = NamingStrategy.IDENTITY;
    this.hasSchemaName = false;
    this.parameterRegistration = ParameterRegistration.INDEX_ONLY;
    this.exceptionAdapter = getDefaultExceptionAdapter(dataSource);
    this.typeMapper = DefaultTypeMapper.INSTANCE;
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
            this.parameterRegistration, this.exceptionAdapter, this.typeMapper);
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

    private final TypeMapper typeMapper;

    ProcedureCaller(DataSource dataSource,
            NamingStrategy parameterNamingStrategy,
            NamingStrategy procedureNamingStrategy,
            NamingStrategy schemaNamingStrategy, boolean hasSchemaName,
            ParameterRegistration parameterRegistration,
            SQLExceptionAdapter exceptionAdapter,
            TypeMapper typeMapper) {
      this.dataSource = dataSource;
      this.parameterNamingStrategy = parameterNamingStrategy;
      this.procedureNamingStrategy = procedureNamingStrategy;
      this.schemaNamingStrategy = schemaNamingStrategy;
      this.hasSchemaName = hasSchemaName;
      this.parameterRegistration = parameterRegistration;
      this.exceptionAdapter = exceptionAdapter;
      this.typeMapper = typeMapper;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      Class<?> returnType = getBoxedClass(method.getReturnType());
      CallInfo callInfo = this.buildCallInfo(method, args);
      Object returnValue;
      try (Connection connection = this.dataSource.getConnection()) {
        try (CallableStatement statement = this.prepareCall(connection, callInfo)) {
          bindParameters(args, callInfo, statement);
          returnValue = this.execute(statement, callInfo, returnType);
        }
      } catch (SQLException e) {
        throw translate(e, method, callInfo);
      }
      return checkReturnType(returnType, returnValue);
    }

    private void bindParameters(Object[] args, CallInfo callInfo, CallableStatement statement) throws SQLException {
      this.bindInParameters(statement, callInfo, args);
      if (callInfo.hasOutParamter()) {
        this.bindOutParameter(statement, callInfo);
      }
    }

    private static Object checkReturnType(Class<?> returnType, Object returnValue) {
      if (returnType == void.class) {
        return null;
      }
      return returnType.cast(returnValue);
    }

    private static Class<?> getBoxedClass(Class<?> clazz) {
      if (clazz == void.class) {
        return clazz;
      }
      if (!clazz.isPrimitive()) {
        return clazz;
      } else {
        return getWrapperClass(clazz);
      }
    }

    private static Class<?> getWrapperClass(Class<?> primitiveClass) {
      if (primitiveClass == int.class) {
        return Integer.class;
      } else if (primitiveClass == long.class) {
        return Long.class;
      } else if (primitiveClass == float.class) {
        return Float.class;
      } else if (primitiveClass == double.class) {
        return Double.class;
      } else if (primitiveClass == byte.class) {
        return Byte.class;
      } else if (primitiveClass == short.class) {
        return Short.class;
      } else if (primitiveClass == char.class) {
        return Character.class;
      } else if (primitiveClass == boolean.class) {
        return Boolean.class;
      } else {
        throw new IllegalArgumentException("unknown primitive type: " + primitiveClass);
      }
    }

    private Exception translate(SQLException exception, Method method, CallInfo callInfo) {
      if (wantsExceptionTranslation(method)) {
        return this.exceptionAdapter.translate(callInfo.procedureName, callInfo.callString, exception);
      } else {
        return exception;
      }
    }

    private Object execute(CallableStatement statement, CallInfo callInfo, Class<?> returnType) throws SQLException {
      if (returnType == void.class) {
        return executeVoidMethod(statement);
      } else {
        if (Collection.class.isAssignableFrom(returnType)) {
          // TODO Auto-generated method stub
          throw new IllegalArgumentException("collections not yet implemented");
        }
        return executeScalarMethod(statement, callInfo, returnType);
      }
    }

    private Object executeScalarMethod(CallableStatement statement, CallInfo callInfo, Class<?> returnType) throws SQLException {
      int count = 0;
      Object last = null;
      boolean hasResultSet = statement.execute();

      // TODO bind by name
      try {
        last = statement.getObject(callInfo.outParameterIndex, returnType);
      } catch (SQLFeatureNotSupportedException e) {
        // we need to pass the class for Java 8 Date Time support
        // however the Postgres JDBC driver does not (yet) support this
        last = statement.getObject(callInfo.outParameterIndex);
      }
      count = 1;
//      try (ResultSet rs = statement.executeQuery()) {
//        while (rs.next()) {
//          last = rs.getObject(1);
//          count += 1;
//        }
//      }
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

    private boolean isExtractParameterNames() {
      ParameterRegistration registration = this.parameterRegistration;
      return registration == ParameterRegistration.NAME_ONLY
              || registration == ParameterRegistration.NAME_AND_TYPE;
    }

    private boolean isExtractParameterTypes() {
      ParameterRegistration registration = this.parameterRegistration;
      return registration == ParameterRegistration.INDEX_AND_TYPE
              || registration == ParameterRegistration.NAME_AND_TYPE;
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

    private CallableStatement prepareCall(Connection connection, CallInfo callInfo) throws SQLException {
      return connection.prepareCall(callInfo.callString);
    }

    private CallInfo buildCallInfo(Method method, Object[] args) {
      int parameterCount = getParameterCount(args);
      String procedureName = this.extractProcedureName(method);
      String schemaName = this.hasSchemaName(method) ? this.extractSchemaName(method) : null;
      boolean isFunction = procedureHasReturnValue(method);
      Class<?> methodReturnType = method.getReturnType();
      boolean methodHasReturnValue = methodReturnType != void.class;
      String callString = buildCallString(schemaName, procedureName, parameterCount, isFunction, !methodHasReturnValue);
      int[] inParameterTypes = this.isExtractParameterTypes() ? this.extractParameterTypes(method) : null;
      String[] inParameterNames = this.isExtractParameterNames() ? extractParameterNames(method) : null;

      int outParameterIndex;
      int outParameterType;
      String outParameterName;
      if (methodHasReturnValue) {
        OutParameter outParameter = method.getAnnotation(OutParameter.class);
        ReturnValue returnValue = method.getAnnotation(ReturnValue.class);
        if (outParameter != null) {
          if (returnValue != null) {
            throw new IllegalArgumentException("method " + method + " needs to be annotated with only one of" + OutParameter.class + " or " + ReturnValue.class);
          }
          outParameterName = outParameter.name();
          outParameterIndex = outParameter.index();
          outParameterType = outParameter.type();
        } else if (returnValue != null) {
          outParameterName = returnValue.name();
          outParameterIndex = 1;
          outParameterType = returnValue.type();
        } else {
          throw new IllegalArgumentException("method " + method + " needs to be annotated with " + OutParameter.class + " or " + ReturnValue.class);
        }
        // correct annotation default values
        if (outParameterName.isEmpty()) {
          outParameterName = null;
        }
        if (outParameterIndex == -1) {
          outParameterIndex = parameterCount + 1;
        }
        if (outParameterType == Integer.MIN_VALUE) {
          outParameterType = this.typeMapper.mapToSqlType(methodReturnType);
        }
      } else {
        outParameterIndex = -1;
        outParameterType = 0;
        outParameterName = null;
      }

      int[] inParameterIndices;
      if (!isFunction && outParameterIndex == parameterCount) {
        // we have an out parameter and it's the last parameter
        inParameterIndices = buildInParameterIndices(parameterCount);
      } else {
        inParameterIndices = buildInParameterIndices(parameterCount, outParameterIndex);
      }

      return new CallInfo(procedureName, callString,
              outParameterIndex, outParameterType, outParameterName,
              inParameterIndices, inParameterTypes, inParameterNames);

    }

    static int[] buildInParameterIndices(int parameterCount) {
      int[] indices = new int[parameterCount];
      for (int i = 0; i < indices.length; i++) {
        indices[i] = i + 1;
      }
      return indices;
    }

    static int[] buildInParameterIndices(int parameterCount, int outParameterIndex) {
      int[] indices = new int[parameterCount];
      for (int i = 0; i < indices.length; i++) {
        if (outParameterIndex > i + 1) {
          indices[i] = i + 1;
        } else {
          indices[i] = i + 2;
        }
      }
      return indices;
    }

    private static int getParameterCount(Object[] args) {
      return args != null ? args.length : 0;
    }

    private static String buildCallString(String schemaName, String procedureName, int parameterCount, boolean isFunction, boolean isVoid) {
      if (isFunction) {
        return buildQualifiedFunctionCallString(schemaName, procedureName, parameterCount);
      } else {
        return buildQualifiedProcedureCallString(schemaName, procedureName, isVoid ? parameterCount : parameterCount + 1);
      }
    }

    static String buildQualifiedProcedureCallString(String schemaName, String functionName, int parameterCount) {
      // {call RAISE_PRICE(?,?,?)}
      int capacity = 6; // {call
      if (schemaName != null) {
        capacity += schemaName.length()
                + 1; // .
      }
      capacity += functionName.length()
              + 1 // (
              + Math.max(parameterCount * 2 - 1, 0) // ?,?
              + 2; // )}
      StringBuilder builder = new StringBuilder(capacity);
      builder.append("{call ");
      if (schemaName != null) {
        builder.append(schemaName);
        builder.append('.');
      }
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
      int capacity = 11; // { ? = call
      if (schemaName != null) {
        capacity += schemaName.length()
                + 1; // .
      }
      capacity += functionName.length()
              + 1 // (
              + Math.max(parameterCount * 2 - 1, 0) // ?,?
              + 2; // )}
      StringBuilder builder = new StringBuilder(capacity);
      builder.append("{ ? = call ");
      if (schemaName != null) {
        builder.append(schemaName);
        builder.append('.');
      }
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

    private static boolean procedureHasReturnValue(Method method) {
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

    private boolean hasSchemaName(Method method) {
      return this.hasSchemaName || method.getDeclaringClass().isAnnotationPresent(SchemaName.class);
    }

    private String extractSchemaName(Method method) {
      Class<?> declaringClass = method.getDeclaringClass();
      SchemaName schemaNameAnnotation = declaringClass.getAnnotation(SchemaName.class);
      if (schemaNameAnnotation != null) {
        String schemaName = schemaNameAnnotation.value();
        if (!schemaName.isEmpty()) {
          return schemaName;
        }
      }
      return this.schemaNamingStrategy.translateToDatabase(declaringClass.getName());
    }

    private void bindOutParameter(CallableStatement statement, CallInfo callInfo) throws SQLException {
      switch (this.parameterRegistration) {
        case INDEX_ONLY:
        case INDEX_AND_TYPE:
          this.bindOutParameterByIndex(statement, callInfo);
          break;
        case NAME_ONLY:
        case NAME_AND_TYPE:
          this.bindOutParameterByName(statement, callInfo);
          break;
        default:
          throw new IllegalStateException("unknown parameter registration: " + this.parameterRegistration);
      }
    }

    private void bindOutParameterByIndex(CallableStatement statement, CallInfo callInfo) throws SQLException {
      statement.registerOutParameter(callInfo.outParameterIndex, callInfo.outParameterType);
    }

    private void bindOutParameterByName(CallableStatement statement, CallInfo callInfo) throws SQLException {
      statement.registerOutParameter(callInfo.outParameterName, callInfo.outParameterType);
    }

    private void bindInParameters(CallableStatement statement, CallInfo callInfo, Object[] args) throws SQLException {
      switch (this.parameterRegistration) {
        case INDEX_ONLY:
          this.bindParametersByIndex(statement, callInfo, args);
          break;
        case NAME_ONLY:
          this.bindParametersByName(statement, callInfo, args);
          break;
        case INDEX_AND_TYPE:
          this.bindParametersByIndexAndType(statement, callInfo, args);
          break;
        case NAME_AND_TYPE:
          this.bindParametersByNameAndType(statement, callInfo, args);
          break;
        default:
          throw new IllegalStateException("unknown parameter registration: " + this.parameterRegistration);
      }
    }

    private void bindParametersByIndex(CallableStatement statement, CallInfo callInfo, Object[] args) throws SQLException {
      if (args == null) {
        return;
      }
      for (int i = 0; i < args.length; i++) {
        statement.setObject(callInfo.inParameterIndices[i], args[i]);
      }
    }

    private void bindParametersByIndexAndType(CallableStatement statement, CallInfo callInfo, Object[] args) throws SQLException {
      if (args == null) {
        return;
      }
      for (int i = 0; i < args.length; i++) {
        int parameterIndex = callInfo.inParameterIndices[i];
        Object arg = args[i];
        int type = callInfo.inParameterTypes[i];
        if (arg != null) {
          statement.setObject(parameterIndex, arg, type);
        } else {
          statement.setNull(parameterIndex, type);
        }
      }
    }

    private void bindParametersByName(CallableStatement statement, CallInfo callInfo, Object[] args) throws SQLException {
      if (args == null) {
        return;
      }
      for (int i = 0; i < args.length; i++) {
        statement.setObject(callInfo.inParameterNames[i], args[i]);
      }
    }

    private void bindParametersByNameAndType(CallableStatement statement, CallInfo callInfo, Object[] args) throws SQLException {
      if (args == null) {
        return;
      }
      for (int i = 0; i < args.length; i++) {
        String name = callInfo.inParameterNames[i];
        Object arg = args[i];
        int type = callInfo.inParameterTypes[i];
        if (arg != null) {
          statement.setObject(name, arg, type);
        } else {
          statement.setNull(name, type);
        }
      }
    }

  }

  static final class CallInfo {

    final String procedureName;
    final String callString;
    final int outParameterIndex;
    final int outParameterType;
    final String outParameterName;
    final int[] inParameterIndices;
    final int[] inParameterTypes;
    final String[] inParameterNames;


    CallInfo(String procedureName, String callString, int outParameterIndex,
            int outParameterType, String outParameterName,
            int[] inParameterIndices, int[] inParameterTypes,
            String[] inParameterNames) {
      this.procedureName = procedureName;
      this.callString = callString;
      this.outParameterIndex = outParameterIndex;
      this.outParameterType = outParameterType;
      this.outParameterName = outParameterName;
      this.inParameterIndices = inParameterIndices;
      this.inParameterTypes = inParameterTypes;
      this.inParameterNames = inParameterNames;
    }



    boolean hasOutParamter() {
      return this.outParameterIndex != -1;
    }


  }

}
