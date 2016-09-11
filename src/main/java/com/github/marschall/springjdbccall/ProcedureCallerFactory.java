package com.github.marschall.springjdbccall;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;

import com.github.marschall.springjdbccall.annotations.FetchSize;
import com.github.marschall.springjdbccall.annotations.OutParameter;
import com.github.marschall.springjdbccall.annotations.ParameterName;
import com.github.marschall.springjdbccall.annotations.ParameterType;
import com.github.marschall.springjdbccall.annotations.ProcedureName;
import com.github.marschall.springjdbccall.annotations.ReturnValue;
import com.github.marschall.springjdbccall.annotations.Schema;
import com.github.marschall.springjdbccall.spi.NamingStrategy;
import com.github.marschall.springjdbccall.spi.TypeMapper;

/**
 * Creates instances of an interface containing stored procedure declarations.
 * The instances will call the stored procedure.
 *
 * <p>The instances created by {@link #build()} and
 * {@link #build(Class, DataSource)} should have the life time of the
 * application. They should be reused instead of creating new instances
 * for every call. They are prime candidates for
 * <a href="https://en.wikipedia.org/wiki/Dependency_injection">injection</a>.</p>
 *
 * <p>This class implements the
 * <a href="https://en.wikipedia.org/wiki/Builder_pattern">builder pattern</a>
 * allowing you to override various defaults. If you're fine with the
 * defaults you can create interface instances directly with
 * {@link #build(Class, DataSource)}.</p>
 *
 * <p>Instances of this class are not thread safe but the instances returned by
 * {@link #build()} and {@link #build(Class, DataSource)} are.</p>
 *
 * <h3>Simple Usage</h3>
 * In the simplest case this class can be used like this:
 * <pre><code>
 *  DataSource dataSource = ...; // some way to get the data source, either injection or look up
 *  Class&lt;MyProcedures&gt; inferfaceDeclaration = MyProcedures.class; // your interface containing your procedure declarations
 *  MyProcedures procedures = ProcedureCallerFactory.build(inferfaceDeclaration, dataSource);
 *  procedures.aProcedure("param1", "param2"); // actual procedure with actual parameters
 * </code></pre>
 *
 * <h3>Advanced Usage</h3>
 * If you want to customize the defaults you can use this class like this:
 * <pre><code>
 *  DataSource dataSource = ...; // some way to get the data source, either injection or look up
 *  Class&lt;MyProcedures&gt; inferfaceDeclaration = MyProcedures.class; // your interface containing your procedure declarations
 *  MyProcedures procedures = ProcedureCallerFactory.of(inferfaceDeclaration, dataSource)
 *    .withParameterRegistration(ParameterRegistration.INDEX_AND_TYPE) // change one or multiple defaults
 *    .build();
 *  procedures.aProcedure("param1", "param2"); // actual procedure with actual parameters
 * </code></pre>
 *
 * @param <T> the interface containing the stored procedure declarations
 */
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

  /**
   * Creates a builder for the caller for the interface of stored procedures.
   *
   * @param inferfaceDeclaration the interface containing the store procedure declarations
   * @param dataSource the data source through with to make the calls
   * @param <T> the interface type containing the stored procedure declarations
   * @return the builder for the caller
   */
  public static <T> ProcedureCallerFactory<T> of(Class<T> inferfaceDeclaration, DataSource dataSource) {
    Objects.requireNonNull(inferfaceDeclaration);
    Objects.requireNonNull(dataSource);
    return new ProcedureCallerFactory<>(inferfaceDeclaration, dataSource);
  }

  /**
   * Creates a caller for the interface of stored procedures using the defaults.
   *
   * @param inferfaceDeclaration the interface containing the store procedure declarations
   * @param dataSource the data source through with to make the calls
   * @param <T> the interface type containing the stored procedure declarations
   * @return the interface instance
   */
  public static <T> T build(Class<T> inferfaceDeclaration, DataSource dataSource) {
    return of(inferfaceDeclaration, dataSource).build();
  }

  /**
   * Allows you to use a custom way how parameter names are derived from Java names.
   *
   * <p>The given object is only applied if the parameter registration is either
   * {@link ParameterRegistration#NAME_ONLY} or {@link ParameterRegistration#NAME_AND_TYPE}
   * and {@link ParameterName} is not present.
   * The given object is never applied to an out parameter.
   * Source level parameter names are only available with you compile with
   * <a href="https://docs.oracle.com/javase/tutorial/reflect/member/methodparameterreflection.html">-parameters</a>.</p>
   *
   * @param parameterNamingStrategy the naming strategy for parameters, not {@code null}
   * @return this builder for chaining
   */
  public ProcedureCallerFactory<T> withParameterNamingStrategy(NamingStrategy parameterNamingStrategy) {
    Objects.requireNonNull(parameterNamingStrategy);
    this.parameterNamingStrategy = parameterNamingStrategy;
    return this;
  }

  /**
   * Allows you to use a custom way how procedure names are derived from Java names.
   *
   * <p>The given object is only applied if {@link ProcedureName} is not present.
   *
   * @param procedureNamingStrategy the naming strategy for procedures, not {@code null}
   * @return this builder for chaining
   */
  public ProcedureCallerFactory<T> withProcedureNamingStrategy(NamingStrategy procedureNamingStrategy) {
    Objects.requireNonNull(procedureNamingStrategy);
    this.procedureNamingStrategy = procedureNamingStrategy;
    return this;
  }

  public ProcedureCallerFactory<T> withSchemaNamingStrategy(NamingStrategy schemaNamingStrategy) {
    Objects.requireNonNull(schemaNamingStrategy);
    this.schemaNamingStrategy = schemaNamingStrategy;
    this.hasSchemaName = true;
    return this;
  }

  public ProcedureCallerFactory<T> withSchemaName() {
    this.hasSchemaName = true;
    return this;
  }

  /**
   * Allows you to change the way procedure parameters are registered. The default
   * is {@link ParameterRegistration#INDEX_ONLY}.
   *
   * @param parameterRegistration the parameter registration
   * @return this builder for chaining
   */
  public ProcedureCallerFactory<T> withParameterRegistration(ParameterRegistration parameterRegistration) {
    Objects.requireNonNull(parameterRegistration);
    this.parameterRegistration = parameterRegistration;
    return this;
  }

  /**
   * Allows you to change the way {@link SQLException}s are translated into
   * unchecked exceptions.
   *
   * <p>Only applied if the method is not {@code throws SQLException}. The default
   * if Spring is not present is to use {@link UncheckedSQLException}. The default
   * if Spring is present is to use {@link SQLErrorCodeSQLExceptionTranslator}.</p>
   *
   * @param exceptionAdapter the exception adapter
   * @return this builder for chaining
   */
  public ProcedureCallerFactory<T> withExceptionAdapter(SQLExceptionAdapter exceptionAdapter) {
    Objects.requireNonNull(exceptionAdapter);
    this.exceptionAdapter = exceptionAdapter;
    return this;
  }

  /**
   * Allows you to change the way Java types are translated to SQL types.
   *
   * <p>Only applied if {@link ParameterType}, {@link OutParameter#type()} or
   * {@link ReturnValue#type()} are not present. The default is defined in
   * {@link TypeMapper}.</p>
   *
   * @param typeMapper the type mapper
   * @return this builder for chaining
   */
  public ProcedureCallerFactory<T> withTypeMapper(TypeMapper typeMapper) {
    Objects.requireNonNull(typeMapper);
    this.typeMapper = typeMapper;
    return this;
  }

  /**
   * Creates a caller for the interface of stored procedures using the configured options.
   *
   * @return the interface instance
   */
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

  /**
   * Determines how parameters should be registered.
   */
  public enum ParameterRegistration {

    /**
     * Binds by index only.
     *
     * @see CallableStatement#setObject(int, Object)
     */
    INDEX_ONLY,

    /**
     * Binds by name only.
     *
     * @see CallableStatement#setObject(String, Object)
     */
    NAME_ONLY,

    /**
     * Binds by index and type.
     *
     * @see CallableStatement#setObject(int, Object, int)
     */
    INDEX_AND_TYPE,

    /**
     * Binds by name and type.
     *
     * @see CallableStatement#setObject(int, Object, int)
     */
    NAME_AND_TYPE;
  }

  static final class ProcedureCaller implements InvocationHandler {

    private static final int DEFAULT_FETCH_SIZE = 0;

    private static final int NO_OUT_PARAMTER = -1;

    private final DataSource dataSource;

    private final NamingStrategy parameterNamingStrategy;

    private final NamingStrategy procedureNamingStrategy;

    private final NamingStrategy schemaNamingStrategy;

    private final boolean hasSchemaName;

    private final ParameterRegistration parameterRegistration;

    private final SQLExceptionAdapter exceptionAdapter;

    private final TypeMapper typeMapper;

    /**
     * We assume this is uncontended since we only do a few lookups and gets.
     * Save the memory overhead of a {@link ConcurrentHashMap}.
     */
    private final Map<Method, CallInfo> callInfoCache;

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
      this.callInfoCache = Collections.synchronizedMap(new HashMap<>());
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      CallInfo callInfo = this.getCallInfo(method, args);
      Object returnValue;
      try (Connection connection = this.dataSource.getConnection()) {
        try (CallableStatement statement = this.prepareCall(connection, callInfo)) {
          bindParameters(args, callInfo, statement);
          returnValue = this.execute(statement, callInfo);
        }
      } catch (SQLException e) {
        throw translate(e, callInfo);
      }
      return checkReturnType(callInfo, returnValue);
    }

    private void bindParameters(Object[] args, CallInfo callInfo, CallableStatement statement) throws SQLException {
      this.bindInParameters(statement, callInfo, args);
      if (callInfo.hasOutParamter()) {
        this.bindOutParameter(statement, callInfo);
      }
    }

    private static Object checkReturnType(CallInfo callInfo, Object returnValue) {
      Class<?> boxedReturnType = callInfo.boxedReturnType;
      if (boxedReturnType == void.class) {
        return null;
      }
      return boxedReturnType.cast(returnValue);
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
      // http://stackoverflow.com/questions/38953842/going-from-a-primitive-class-to-a-wrapper-class
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

    private Exception translate(SQLException exception, CallInfo callInfo) {
      if (callInfo.wantsExceptionTranslation) {
        return this.exceptionAdapter.translate(callInfo.procedureName, callInfo.callString, exception);
      } else {
        return exception;
      }
    }

    private Object execute(CallableStatement statement, CallInfo callInfo) throws SQLException {
      Class<?> boxedReturnType = callInfo.boxedReturnType;
      if (boxedReturnType == void.class) {
        return executeVoidMethod(statement);
      } else {
        if (callInfo.isList) {
          return readListFromResultSet(statement, callInfo);
        }
        return executeScalarMethod(statement, callInfo, boxedReturnType);
      }
    }

    private Object executeScalarMethod(CallableStatement statement, CallInfo callInfo, Class<?> returnType) throws SQLException {
      // REVIEW for functions does retrieving the value by name make sense?
      boolean hasResultSet = statement.execute();
      if (hasResultSet) {
        return readFromResultSet(statement, callInfo);
      } else {
        return readFromStatement(statement, callInfo, returnType);
      }
    }

    private Object readFromStatement(CallableStatement statement, CallInfo callInfo, Class<?> returnType) throws SQLException {
      try {
        if (this.isUseParameterNames()) {
          return statement.getObject(callInfo.outParameterName, returnType);
        } else {
          return statement.getObject(callInfo.outParameterIndex, returnType);
        }
      } catch (SQLFeatureNotSupportedException e) {
        // we need to pass the class for Java 8 Date Time support
        // however the PostgreS JDBC driver does not support this
        // so lets try again and hope it works this time
        if (this.isUseParameterNames()) {
          return statement.getObject(callInfo.outParameterName);
        } else {
          return statement.getObject(callInfo.outParameterIndex);
        }
      }
    }

    private Object readFromResultSet(CallableStatement statement, CallInfo callInfo) throws SQLException {
      Object last = null;
      int count = 0;
      // hack for H2 which doesn't have out parameters
      try (ResultSet rs = statement.executeQuery()) {
        while (rs.next()) {
          if (this.isUseParameterNames()) {
//              last = rs.getObject(callInfo.outParameterName, returnType);
            // H2 supports #getObject(String, Class) but always returns null
            last = rs.getObject(callInfo.outParameterName);
          } else {
//              last = rs.getObject(1, returnType);
            // H2 supports #getObject(int, Class) but always returns null
            last = rs.getObject(1);
          }
          count += 1;
        }
        if (count != 1) {
          ProcedureCallerFactory.newIncorrectResultSizeException(1, count);
        }
      }
      return last;
    }

    private Object readListFromResultSet(CallableStatement statement, CallInfo callInfo) throws SQLException {
      int fetchSize = callInfo.fetchSize;
      if (fetchSize != DEFAULT_FETCH_SIZE) {
        statement.setFetchSize(fetchSize);
      }
      boolean hasResultSet = statement.execute();
      List<Object> result = new ArrayList<>();
      if (hasResultSet) {
        try (ResultSet rs = statement.getResultSet()) {
          while (rs.next()) {
            Object element = rs.getObject(1, callInfo.listElementType);
            result.add(element);
          }
        }
      } else {
        if (!callInfo.hasOutParamter()) {
          throw new IllegalStateException("@" + OutParameter.class + " for @" + ReturnValue.class + "  missing");
        }
        try (ResultSet rs = this.getOutResultSet(statement, callInfo)) {
          while (rs.next()) {
            Object element = rs.getObject(1, callInfo.listElementType);
            result.add(element);
          }
        }
      }
      return result;
    }

    private ResultSet getOutResultSet(CallableStatement statement, CallInfo callInfo) throws SQLException {
      if (this.isUseParameterNames()) {
        try {
          return statement.getObject(callInfo.outParameterName, ResultSet.class);
        } catch (SQLFeatureNotSupportedException e) {
          // Postgres hack
          return (ResultSet) statement.getObject(callInfo.outParameterName);
        }
      } else {
        try {
          return statement.getObject(callInfo.outParameterIndex, ResultSet.class);
        } catch (SQLFeatureNotSupportedException e) {
          // Postgres hack
          return (ResultSet) statement.getObject(callInfo.outParameterIndex);
        }
      }
    }

    private Object executeVoidMethod(CallableStatement statement) throws SQLException {
      boolean hasResultSet = statement.execute();
      if (hasResultSet) {
        int count = 0;
        try (ResultSet rs = statement.executeQuery()) {
          while (rs.next()) {
            count += 1;
          }
        }
        // don't check count H2 just returns NULL
      }
      return null;
    }

    private boolean isUseParameterNames() {
      ParameterRegistration registration = this.parameterRegistration;
      return registration == ParameterRegistration.NAME_ONLY
              || registration == ParameterRegistration.NAME_AND_TYPE;
    }

    private boolean isUseParameterTypes() {
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
        } else if (parameter.isNamePresent()) {
          parameterName = this.parameterNamingStrategy.translateToDatabase(parameter.getName());
        } else {
          throw new IllegalArgumentException(parameterNameMissingMessage(method, i));
        }
        names[i] = parameterName;
      }
      return names;
    }

    private static String parameterNameMissingMessage(Method method, int i) {
      return "can't deduce name for parameter " + i + " in " + method
              + " either use "  + ParameterName.class + " or compile with -parameters";
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
          type = this.typeMapper.mapToSqlType(parameter.getType());
        }
        types[i] = type;
      }
      return types;
    }

    private CallableStatement prepareCall(Connection connection, CallInfo callInfo) throws SQLException {
      return connection.prepareCall(callInfo.callString);
    }


    private CallInfo getCallInfo(Method method, Object[] args) {
      CallInfo callInfo = this.callInfoCache.get(method);
      if (callInfo != null) {
        return callInfo;
      }
      // potentially compute callInfo multiple times
      // rather than locking for a long time
      callInfo = this.buildCallInfo(method, args);
      CallInfo previous = this.callInfoCache.putIfAbsent(method, callInfo);
      return previous != null ? previous : callInfo;
    }

    private CallInfo buildCallInfo(Method method, Object[] args) {
      int parameterCount = getParameterCount(method);
      String procedureName = this.extractProcedureName(method);
      String schemaName = this.hasSchemaName(method) ? this.extractSchemaName(method) : null;
      boolean isFunction = procedureHasReturnValue(method);
      Class<?> methodReturnType = method.getReturnType();
      boolean methodHasReturnValue = methodReturnType != void.class;
      int[] inParameterTypes = this.isUseParameterTypes() ? this.extractParameterTypes(method) : null;
      String[] inParameterNames = this.isUseParameterNames() ? extractParameterNames(method) : null;

      int outParameterIndex;
      int outParameterType;
      String outParameterName;
      boolean isList;
      Class<?> listElementType;
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
          // we will use a ResultSet instead of an out parameter or function return value
          // eg for Mysql or H2
          outParameterIndex = NO_OUT_PARAMTER;
          outParameterName = null;
          outParameterType = Integer.MIN_VALUE;
        }
        // correct annotation default values
        if (outParameterName != null && outParameterName.isEmpty()) {
          outParameterName = null;
        }
        if (outParameterIndex == NO_OUT_PARAMTER && (outParameter != null || returnValue != null)) {
          // default for the out parameter index is the last index
          // but only if we use an out parameter for function return value
          // if we use a result set then we have no out parameter
          outParameterIndex = parameterCount + 1;
        }
        isList = methodReturnType == List.class;
        if (outParameterType == Integer.MIN_VALUE) {
          if (isList) {
            outParameterType = Types.REF_CURSOR;
          } else {
            outParameterType = this.typeMapper.mapToSqlType(methodReturnType);
          }
        }
        if (isList) {
          listElementType = getListReturnTypeParamter(method);
        } else {
          listElementType = null;
        }
      } else {
        outParameterIndex = NO_OUT_PARAMTER;
        outParameterType = 0;
        outParameterName = null;
        isList = false;
        listElementType = null;
      }

      int[] inParameterIndices;
      boolean hasOutParameter = outParameterIndex != NO_OUT_PARAMTER;
      if (!hasOutParameter || (hasOutParameter && outParameterIndex == parameterCount + 1)) {
        // we have an no out parameter or
        // we have an out parameter and it's the last parameter
        inParameterIndices = buildInParameterIndices(parameterCount);
      } else {
        inParameterIndices = buildInParameterIndices(parameterCount, outParameterIndex);
      }


      String callString = buildCallString(schemaName, procedureName, parameterCount, isFunction, hasOutParameter);
      boolean wantsExceptionTranslation = wantsExceptionTranslation(method);
      Class<?> boxedReturnType = getBoxedClass(method.getReturnType());

      return new CallInfo(procedureName, callString,
              outParameterIndex, outParameterType, outParameterName,
              inParameterIndices, inParameterTypes, inParameterNames,
              wantsExceptionTranslation, boxedReturnType,
              isList, listElementType,
              getFetchSize(method));

    }

    private static int getFetchSize(Method method) {
      if (method.isAnnotationPresent(FetchSize.class)) {
        return method.getAnnotation(FetchSize.class).value();
      }
      Class<?> declaringClass = method.getDeclaringClass();
      if (declaringClass.isAnnotationPresent(FetchSize.class)) {
        return declaringClass.getAnnotation(FetchSize.class).value();
      }
      return DEFAULT_FETCH_SIZE;
    }

    private static Class<?> getListReturnTypeParamter(Method method) {
      Type genericReturnType = method.getGenericReturnType();
      if (genericReturnType instanceof ParameterizedType) {
        ParameterizedType pt = (ParameterizedType) genericReturnType;
        Type[] actualTypeArguments = pt.getActualTypeArguments();
        if (actualTypeArguments.length != 1) {
          throw new IllegalArgumentException("type arguments return type of " + method + " are missing");
        }
        Type actualTypeArgument = actualTypeArguments[0];
        if (!(actualTypeArgument instanceof Class)) {
          throw new IllegalArgumentException("type arguments return type of " + method + " is not a class");
        }
        return (Class<?>) actualTypeArgument;
      } else {
        throw new IllegalArgumentException("method " + method + " is missing type paramter for " + List.class);
      }
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

    private static int getParameterCount(Method method) {
      return method.getParameterCount();
    }

    private static String buildCallString(String schemaName, String procedureName, int parameterCount, boolean isFunction, boolean hasOutParameter) {
      if (isFunction) {
        return buildQualifiedFunctionCallString(schemaName, procedureName, parameterCount);
      } else {
        return buildQualifiedProcedureCallString(schemaName, procedureName, hasOutParameter ? parameterCount + 1 : parameterCount);
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
      return this.hasSchemaName || method.getDeclaringClass().isAnnotationPresent(Schema.class);
    }

    private String extractSchemaName(Method method) {
      Class<?> declaringClass = method.getDeclaringClass();
      Schema schemaNameAnnotation = declaringClass.getAnnotation(Schema.class);
      if (schemaNameAnnotation != null) {
        String schemaName = schemaNameAnnotation.value();
        if (!schemaName.isEmpty()) {
          return schemaName;
        }
      }
      return this.schemaNamingStrategy.translateToDatabase(declaringClass.getSimpleName());
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
        // REVIEW null check?
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
        // REVIEW null check?
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
    final boolean wantsExceptionTranslation;
    final int fetchSize;

    /**
     * Instead of {@code int.class} contains {@code Integer.class}.
     */
    final Class<?> boxedReturnType;
    final boolean isList;
    final Class<?> listElementType;

    CallInfo(String procedureName, String callString, int outParameterIndex,
            int outParameterType, String outParameterName,
            int[] inParameterIndices, int[] inParameterTypes,
            String[] inParameterNames, boolean wantsExceptionTranslation,
            Class<?> boxedReturnType,
            boolean isList, Class<?> listElementType,
            int fetchSize) {
      this.procedureName = procedureName;
      this.callString = callString;
      this.outParameterIndex = outParameterIndex;
      this.outParameterType = outParameterType;
      this.outParameterName = outParameterName;
      this.inParameterIndices = inParameterIndices;
      this.inParameterTypes = inParameterTypes;
      this.inParameterNames = inParameterNames;
      this.wantsExceptionTranslation = wantsExceptionTranslation;
      this.boxedReturnType = boxedReturnType;
      this.isList = isList;
      this.listElementType = listElementType;
      this.fetchSize = fetchSize;
    }

    boolean hasOutParamter() {
      return this.outParameterIndex != ProcedureCaller.NO_OUT_PARAMTER;
    }

  }

}
