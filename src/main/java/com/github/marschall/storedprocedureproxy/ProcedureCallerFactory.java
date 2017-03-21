package com.github.marschall.storedprocedureproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.sql.DataSource;

import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;

import com.github.marschall.storedprocedureproxy.annotations.FetchSize;
import com.github.marschall.storedprocedureproxy.annotations.InOutParameter;
import com.github.marschall.storedprocedureproxy.annotations.Namespace;
import com.github.marschall.storedprocedureproxy.annotations.OutParameter;
import com.github.marschall.storedprocedureproxy.annotations.ParameterName;
import com.github.marschall.storedprocedureproxy.annotations.ParameterType;
import com.github.marschall.storedprocedureproxy.annotations.ProcedureName;
import com.github.marschall.storedprocedureproxy.annotations.ReturnValue;
import com.github.marschall.storedprocedureproxy.annotations.Schema;
import com.github.marschall.storedprocedureproxy.annotations.TypeName;
import com.github.marschall.storedprocedureproxy.spi.NamingStrategy;
import com.github.marschall.storedprocedureproxy.spi.TypeMapper;
import com.github.marschall.storedprocedureproxy.spi.TypeNameResolver;

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

  private static final TypeNameResolver DEFAULT_TYPE_NAME_RESOLVER = new DelegatingTypeNameResolver(DefaultTypeNameResolver.INSTANCE);

  static {
    boolean hasSpring;
    IncorrectResultSizeExceptionGenerator incorrectResultSizeExceptionGenerator;
    try {
      Class.forName("org.springframework.jdbc.support.SQLExceptionTranslator", false, ProcedureCallerFactory.class.getClassLoader());
      hasSpring = true;
      incorrectResultSizeExceptionGenerator = new SpringIncorrectResultSizeExceptionGenerator();
    } catch (ClassNotFoundException e) {
      hasSpring = false;
      incorrectResultSizeExceptionGenerator = new DefaultIncorrectResultSizeExceptionGenerator();
    }
    HAS_SPRING = hasSpring;
    INCORRECT_RESULT_SIZE_EXCEPTION_GENERATOR = incorrectResultSizeExceptionGenerator;
  }

  private final Class<T> interfaceDeclaration;

  private final DataSource dataSource;

  private NamingStrategy parameterNamingStrategy;

  private NamingStrategy procedureNamingStrategy;

  private NamingStrategy schemaNamingStrategy;

  private NamingStrategy namespaceNamingStrategy;

  private boolean hasSchema;

  private boolean hasNamespace;

  private ParameterRegistration parameterRegistration;

  private SQLExceptionAdapter exceptionAdapter;

  private TypeMapper typeMapper;

  private TypeNameResolver typeNameResolver;

  private boolean useOracleArrays;

  private ProcedureCallerFactory(Class<T> interfaceDeclaration, DataSource dataSource) {
    this.interfaceDeclaration = interfaceDeclaration;
    this.dataSource = dataSource;
    this.parameterNamingStrategy = NamingStrategy.IDENTITY;
    this.procedureNamingStrategy = NamingStrategy.IDENTITY;
    this.schemaNamingStrategy = NamingStrategy.IDENTITY;
    this.namespaceNamingStrategy = NamingStrategy.IDENTITY;
    this.hasSchema = false;
    this.hasNamespace = false;
    this.parameterRegistration = ParameterRegistration.INDEX_ONLY;
    this.exceptionAdapter = getDefaultExceptionAdapter(dataSource);
    this.typeMapper = DefaultTypeMapper.INSTANCE;
    this.typeNameResolver = DEFAULT_TYPE_NAME_RESOLVER;
    this.useOracleArrays = false;
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

  /**
   * Causes a schema name to be added to the call string by applying
   * the given function to the interface name.
   *
   * @param schemaNamingStrategy the naming strategy for schemas, not {@code null}
   * @return this builder for chaining
   */
  public ProcedureCallerFactory<T> withSchemaNamingStrategy(NamingStrategy schemaNamingStrategy) {
    Objects.requireNonNull(schemaNamingStrategy);
    this.schemaNamingStrategy = schemaNamingStrategy;
    this.hasSchema = true;
    return this;
  }

  /**
   * Causes a schema name to be added to the call string.
   *
   * <p>Per default the interface name is used.</p>
   *
   * @return this builder for chaining
   * @see Schema
   * @see ProcedureCallerFactory#withSchemaNamingStrategy(NamingStrategy)
   */
  public ProcedureCallerFactory<T> withSchema() {
    this.hasSchema = true;
    return this;
  }

  /**
   * Causes a namespace to be added to the call string by applying
   * the given function to the interface name.
   *
   * @param namespaceNamingStrategy the naming strategy for namespaces, not {@code null}
   * @return this builder for chaining
   */
  public ProcedureCallerFactory<T> withNamespaceNamingStrategy(NamingStrategy namespaceNamingStrategy) {
    Objects.requireNonNull(namespaceNamingStrategy);
    this.namespaceNamingStrategy = namespaceNamingStrategy;
    this.hasNamespace = true;
    return this;
  }

  /**
   * Causes a namespace to be added to the call string.
   *
   * <p>Per default the interface name is used.</p>
   *
   * @return this builder for chaining
   * @see Namespace
   * @see ProcedureCallerFactory#withNamespaceNamingStrategy(NamingStrategy)
   */
  public ProcedureCallerFactory<T> withNamespace() {
    this.hasNamespace = true;
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
   * Allows you to change the way SQL type names for array elements are resolved.
   *
   * <p>Only applied if {@link TypeName} is not present.</p>
   *
   * @param typeNameResolver the type name resolver
   * @return this builder for chaining
   */
  public ProcedureCallerFactory<T> withTypeNameResolver(TypeNameResolver typeNameResolver) {
    Objects.requireNonNull(typeNameResolver);
    this.typeNameResolver = new DelegatingTypeNameResolver(typeNameResolver);
    return this;
  }

  /**
   * Uses Oracle API to create arrays.
   *
   * @return this builder for chaining
   */
  public ProcedureCallerFactory<T> withOracleArrays() {
    this.useOracleArrays = true;
    return this;
  }

  /**
   * Uses Oracle JDBC types.
   *
   * <p>Currently only required if you use boolean types.</p>
   *
   * @return this builder for chaining
   */
  public ProcedureCallerFactory<T> withOracleTypeMapper() {
    return withTypeMapper(OracleTypeMapper.INSTANCE);
  }

  /**
   * Enables all Oracle extensions.
   *
   * <p>Currently only required for:</p>
   * <ul>
   *  <li>boolean types</li>
   *  <li>arrays</li>
   * </ul>
   *
   * @return this builder for chaining
   */
  public ProcedureCallerFactory<T> withOracleExtensions() {
    this.withOracleArrays();
    return withOracleTypeMapper();
  }

  /**
   * Creates a caller for the interface of stored procedures using the configured options.
   *
   * @return the interface instance
   */
  public T build() {
    ProcedureCaller caller = new ProcedureCaller(this.dataSource, this.interfaceDeclaration,
            this.parameterNamingStrategy, this.procedureNamingStrategy, this.schemaNamingStrategy,
            this.hasSchema,
            this.namespaceNamingStrategy, this.hasNamespace,
            this.parameterRegistration, this.exceptionAdapter,
            this.typeMapper, this.typeNameResolver,
            this.useOracleArrays);
    // REVIEW correct class loader
    Object proxy = Proxy.newProxyInstance(this.interfaceDeclaration.getClassLoader(),
            new Class<?>[]{this.interfaceDeclaration}, caller);
    return this.interfaceDeclaration.cast(proxy);
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

    static final int DEFAULT_FETCH_SIZE = 0;

    // an interface method can not have more than 254 parameters
    private static final int NO_OUT_PARAMTER = -1;

    private static final int NO_VALUE_EXTRACTOR = -1;

    /**
     * The method argument at this index is not an in parameter. It may
     * be a {@link ValueExtractor} or {@link NumberedValueExtractor}.
     *
     * 0 is not a valid parameter index.
     */
    static final int NO_IN_PARAMTER = 0;

    private final DataSource dataSource;

    private final Class<?> interfaceDeclaration;

    private final NamingStrategy parameterNamingStrategy;

    private final NamingStrategy procedureNamingStrategy;

    private final NamingStrategy schemaNamingStrategy;

    private final NamingStrategy namespaceNamingStrategy;

    private final boolean hasSchema;

    private final boolean hasNamespace;

    private final ParameterRegistration parameterRegistration;

    private final SQLExceptionAdapter exceptionAdapter;

    private final TypeMapper typeMapper;

    private final TypeNameResolver typeNameResolver;

    /**
     * We assume this is uncontended since we only do a few lookups and gets.
     * Save the memory overhead of a {@link ConcurrentHashMap}.
     */
    private final Map<Method, CallInfo> callInfoCache;

    private final ReadWriteLock cacheLock;

    private final boolean useOracleArrays;

    ProcedureCaller(DataSource dataSource,
            Class<?> interfaceDeclaration,
            NamingStrategy parameterNamingStrategy,
            NamingStrategy procedureNamingStrategy,
            NamingStrategy schemaNamingStrategy, boolean hasSchemaName,
            NamingStrategy namespaceNamingStrategy, boolean hasNamespace,
            ParameterRegistration parameterRegistration,
            SQLExceptionAdapter exceptionAdapter,
            TypeMapper typeMapper,
            TypeNameResolver typeNameResolver,
            boolean useOracleArrays) {
      this.dataSource = dataSource;
      this.interfaceDeclaration = interfaceDeclaration;
      this.parameterNamingStrategy = parameterNamingStrategy;
      this.procedureNamingStrategy = procedureNamingStrategy;
      this.schemaNamingStrategy = schemaNamingStrategy;
      this.hasSchema = hasSchemaName;
      this.namespaceNamingStrategy = namespaceNamingStrategy;
      this.hasNamespace = hasNamespace;
      this.parameterRegistration = parameterRegistration;
      this.exceptionAdapter = exceptionAdapter;
      this.typeMapper = typeMapper;
      this.typeNameResolver = typeNameResolver;
      this.useOracleArrays = useOracleArrays;
      this.callInfoCache = new HashMap<>();
      this.cacheLock = new ReentrantReadWriteLock();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if (method.isDefault()) {
        throw new IllegalStateException("default methods are not supported");
      }

      // handle methods defined in Object: toString, hashCode, equals
      String methodName = method.getName();
      int argCount = args == null ? 0 : args.length;
      if (methodName.equals("toString") && argCount == 0) {
        return "stored procedures defined in " + this.interfaceDeclaration.getName();
      } else if (methodName.equals("hashCode") && argCount == 0) {
        return System.identityHashCode(proxy);
      } else if (methodName.equals("equals") && argCount == 1) {
        return proxy == args[0];
      }

      // handle actual interface methods
      CallInfo callInfo = this.getCallInfo(method, args);
      try (Connection connection = this.dataSource.getConnection()) {
        try (CallResource callResource = callInfo.callResourceFactory.createResource(connection, args);
             CallableStatement statement = prepareCall(connection, callInfo)) {
          bindParameters(args, callInfo, statement, callResource);
          return execute(statement, callInfo, args);
        }
      } catch (SQLException e) {
        throw translate(e, callInfo);
      }
    }

    private static void bindParameters(Object[] args, CallInfo callInfo, CallableStatement statement, CallResource callResource) throws SQLException {
      callInfo.outParameterRegistration.bindOutParamter(statement);
      callInfo.inParameterRegistration.bindInParamters(statement, callResource, args);
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

    private static Object execute(CallableStatement statement, CallInfo callInfo, Object[] args) throws SQLException {
      return callInfo.resultExtractor.extractResult(statement, callInfo.outParameterRegistration, args);
    }

    private String[] extractParameterNames(Method method) {
      Parameter[] parameters = method.getParameters();
      String[] names = new String[parameters.length];
      for (int i = 0; i < parameters.length; i++) {
        Parameter parameter = parameters[i];
        names[i] = getParameterName(parameter, method, i);
      }
      return names;
    }

    private String getParameterName(Parameter parameter, Method method, int parameterIndex) {
      if (ValueExtractorUtils.isAnyValueExtractor(parameter.getType())) {
        return null; // default value of uninitialized array
      }

      ParameterName annotation = parameter.getAnnotation(ParameterName.class);
      if (annotation != null) {
        return annotation.value();
      } else if (parameter.isNamePresent()) {
        return this.parameterNamingStrategy.translateToDatabase(parameter.getName());
      } else {
        throw new IllegalArgumentException(parameterNameMissingMessage(method, parameterIndex));
      }
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
        types[i] = getParameterType(parameter);
      }
      return types;
    }

    private int getParameterType(Parameter parameter) {
      Class<?> parameterType = parameter.getType();
      if (ValueExtractorUtils.isAnyValueExtractor(parameterType)) {
        return 0; // default value of uninitialized array
      }

      ParameterType annotation = parameter.getAnnotation(ParameterType.class);
      if (annotation != null) {
        return annotation.value();
      } else {
        return this.typeMapper.mapToSqlType(parameterType);
      }
    }

    private static CallableStatement prepareCall(Connection connection, CallInfo callInfo) throws SQLException {
      return connection.prepareCall(callInfo.callString);
    }


    private CallInfo getCallInfo(Method method, Object[] args) {
      CallInfo callInfo = getFromCacheOrNull(method);
      if (callInfo != null) {
        return callInfo;
      }

      // potentially compute callInfo multiple times
      // rather than locking for a long time
      callInfo = this.buildCallInfo(method, args);

      CallInfo previous = tryWriteToCache(method, callInfo);
      return previous != null ? previous : callInfo;
    }

    private CallInfo tryWriteToCache(Method method, CallInfo callInfo) {
      Lock lock = this.cacheLock.writeLock();
      lock.lock();
      try {
        return this.callInfoCache.putIfAbsent(method, callInfo);
      } finally {
        lock.unlock();
      }
    }

    private CallInfo getFromCacheOrNull(Method method) {
      Lock lock = this.cacheLock.readLock();
      lock.lock();
      try {
        return this.callInfoCache.get(method);
      } finally {
        lock.unlock();
      }
    }

    private CallInfo buildCallInfo(Method method, Object[] args) {
      int sqlInputParameterCount = getInputParameterCount(method);
      String procedureName = this.extractProcedureName(method);
      Class<?> methodReturnType = method.getReturnType();

      int outParameterSqlIndex = getOutParameterSqlIndex(method);
      boolean hasOutParameter = outParameterSqlIndex != NO_OUT_PARAMTER;

      InParameterRegistration inParameterRegistration = buildInParameterRegistration(
              method, sqlInputParameterCount, outParameterSqlIndex);

      OutParameterRegistration outParameterRegistration = buildOutParameterRegistration(
              method, outParameterSqlIndex, hasOutParameter);

      CallResourceFactory callResourceFactory = buildCallResourceFactory(method);

      String callString = buildCallString(method,
              procedureName, sqlInputParameterCount, hasOutParameter);
      boolean wantsExceptionTranslation = wantsExceptionTranslation(method);
      ResultExtractor resultExtractor = buildResultExtractor(method, methodReturnType);

      return new CallInfo(procedureName, callString,
              wantsExceptionTranslation, resultExtractor, outParameterRegistration,
              inParameterRegistration, callResourceFactory);

    }

    private CallResourceFactory buildCallResourceFactory(Method method) {
      int arrayCount = 0;
      for (Class<?> parameterType : method.getParameterTypes()) {
        if (isCollection(parameterType)) {
          arrayCount += 1;
        }
      }
      if (arrayCount == 0) {
        return NoResourceFactory.INSTANCE;
      } else if (arrayCount == 1) {
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
          Parameter parameter = parameters[i];
          if (isCollection(parameter.getType())) {
            return createArrayResourceFactory(parameter, i);
          }
        }
        throw new AssertionError("inconsistent state we checked for an array but found none");
      } else {
        CallResourceFactory[] factories = new CallResourceFactory[arrayCount];
        int factoryIndex = 0;
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
          Parameter parameter = parameters[i];
          if (isCollection(parameter.getType())) {
            factories[factoryIndex++] = createArrayResourceFactory(parameter, i);
          }
        }
        return new CompositeFactory(factories);
      }
    }

    private static boolean isCollection(Class<?> parameterType) {
      return parameterType.isArray() || Collection.class.isAssignableFrom(parameterType);
    }

    private CallResourceFactory createArrayResourceFactory(Parameter parameter, int parameterIndex) {
      String typeName = this.typeNameResolver.resolveTypeName(parameter);
      if (this.useOracleArrays) {
        return new OracleArrayFactory(parameterIndex, typeName);
      } else {
        return new ArrayFactory(parameterIndex, typeName);
      }
    }

    private InParameterRegistration buildInParameterRegistration(Method method, int sqlParameterCount, int outParameterSqlIndex) {
      boolean hasOutParameter = !method.isAnnotationPresent(InOutParameter.class) && outParameterSqlIndex != NO_OUT_PARAMTER;
      if (sqlParameterCount > 0) {
        switch (this.parameterRegistration) {
          case INDEX_ONLY: {
            int valueExtractorIndex = getValueExtractorIndex(method);
            int javaParameterCount = method.getParameterCount();
            if (valueExtractorIndex == NO_VALUE_EXTRACTOR) {
              if (hasOutParameter && outParameterSqlIndex == 1) {
                return PrefixByIndexInParameterRegistration.INSTANCE;
              }
              if (!hasOutParameter || outParameterSqlIndex == javaParameterCount + 1) {
                return SuffixByIndexInParameterRegistration.INSTANCE;
              }
            }
            byte[] inParameterIndices = buildInParameterIndices(method, javaParameterCount, hasOutParameter, outParameterSqlIndex);
            return new ByIndexInParameterRegistration(inParameterIndices);
          }
          case INDEX_AND_TYPE: {
            int javaParameterCount = method.getParameterCount();
            byte[] inParameterIndices = buildInParameterIndices(method, javaParameterCount, hasOutParameter, outParameterSqlIndex);
            int[] inParameterTypes = this.extractParameterTypes(method);
            return new ByIndexAndTypeInParameterRegistration(inParameterIndices, inParameterTypes);
          }
          case NAME_ONLY: {
            String[] inParameterNames = extractParameterNames(method);
            return new ByNameInParameterRegistration(inParameterNames);
          }
          case NAME_AND_TYPE: {
            String[] inParameterNames = extractParameterNames(method);
            int[] inParameterTypes = this.extractParameterTypes(method);
            return new ByNameAndTypeInParameterRegistration(inParameterNames, inParameterTypes);
          }
          default:
            throw new IllegalStateException("unknown parameter registration: " + this.parameterRegistration);
        }
      } else {
        return NoInParameterRegistration.INSTANCE;
      }
    }

    private ResultExtractor buildResultExtractor(Method method, Class<?> methodReturnType) {
      boolean methodHasReturnValue = methodReturnType != void.class;
      boolean isList = methodHasReturnValue && methodReturnType == List.class;
      boolean isArray = methodHasReturnValue && methodReturnType.isArray();
      if (!methodHasReturnValue) {
        return VoidResultExtractor.INSTANCE;
      } else if (isList) {
        int valueExtractorIndex = getValueExtractorIndex(method);
        int fetchSize = getFetchSize(method);
        if (valueExtractorIndex == NO_VALUE_EXTRACTOR) {
          Class<?> listElementType = getListReturnTypeParamter(method);
          return new ListResultExtractor(listElementType, fetchSize);
        } else {
          Class<?> parameterType = method.getParameterTypes()[valueExtractorIndex];
          if (ValueExtractorUtils.isValueExtractor(parameterType)) {
            return new ValueExtractorResultExtractor(valueExtractorIndex, fetchSize);
          } else if (ValueExtractorUtils.isNumberedValueExtractor(parameterType)) {
            return new NumberedValueExtractorResultExtractor(valueExtractorIndex, fetchSize);
//          } else if (ValueExtractorUtils.isFunction(parameterType)) {
//            return new ValueExtractorResultExtractor(valueExtractorIndex, fetchSize);
          } else {
            throw new IllegalStateException("unknown type of value extractor: " + parameterType);
          }
        }
      } else if (isArray) {
        Class<?> componentType = methodReturnType.getComponentType();
        if (this.useOracleArrays && OracleArrayResultExtractor.isSupportedElementType(componentType)) {
          return new OracleArrayResultExtractor(componentType);
        }
        return new ArrayResultExtractor(componentType);
      } else {
        Class<?> boxedReturnType = getBoxedClass(method.getReturnType());
        return new ScalarResultExtractor(boxedReturnType);
      }
    }

    private static byte[] buildInParameterIndices(Method method, int javaParameterCount, boolean hasOutParameter, int outParameterSqlIndex) {
      Class<?>[] methodParameterTypes = method.getParameterTypes();
      if (!hasOutParameter || (hasOutParameter && outParameterSqlIndex == javaParameterCount + 1)) {
        // we have an no out parameter or
        // we have an out parameter and it's the last parameter
        return buildInParameterIndices(javaParameterCount, methodParameterTypes);
      } else {
        return buildInParameterIndices(javaParameterCount, outParameterSqlIndex, methodParameterTypes);
      }
    }

    private OutParameterRegistration buildOutParameterRegistration(
            Method method, int outParameterSqlIndex, boolean hasOutParameter) {
      InOutParameter inOutParameter = method.getAnnotation(InOutParameter.class);
      if (hasOutParameter) {
        if (inOutParameter == null) {
          int outParameterType = this.getOutParameterType(method);
          String returnTypeName = getReturnTypeName(method);
          switch (this.parameterRegistration) {
            case INDEX_ONLY:
            case INDEX_AND_TYPE:
              return createIndexedOutParameterRegistration(outParameterSqlIndex, outParameterType, returnTypeName);
            case NAME_ONLY:
            case NAME_AND_TYPE:
              String outParameterName = getOutParameterName(method);
              return createNamedOutParameterRegistration(outParameterType, returnTypeName, outParameterName);
            default:
              throw new IllegalStateException("unknown parameter registration: " + this.parameterRegistration);
          }
        } else {
          int outParameterIndex = outParameterSqlIndex - 1;
          Parameter parameter = method.getParameters()[outParameterSqlIndex - 1];
          int outParameterType = this.getParameterType(parameter);
          String typeName;
          if (isCollection(parameter.getType())) {
            typeName = this.typeNameResolver.resolveTypeName(parameter);
          } else {
            typeName = null;
          }

          switch (this.parameterRegistration) {
            case INDEX_ONLY:
            case INDEX_AND_TYPE:
              return createIndexedOutParameterRegistration(outParameterSqlIndex, outParameterType, typeName);
            case NAME_ONLY:
            case NAME_AND_TYPE:
              String outParameterName = getParameterName(parameter, method, outParameterIndex);
              return createNamedOutParameterRegistration(outParameterType, typeName, outParameterName);
            default:
              throw new IllegalStateException("unknown parameter registration: " + this.parameterRegistration);
          }
        }
      } else {
        return NoOutParameterRegistration.INSTANCE;
      }
    }

    private static OutParameterRegistration createNamedOutParameterRegistration(int outParameterType, String returnTypeName, String outParameterName) {
      if (returnTypeName == null) {
        return new ByNameOutParameterRegistration(outParameterName, outParameterType);
      } else {
        return new ByNameAndTypeNameOutParameterRegistration(outParameterName, outParameterType, returnTypeName);
      }
    }

    private static OutParameterRegistration createIndexedOutParameterRegistration(int outParameterSqlIndex, int outParameterType, String returnTypeName) {
      if (returnTypeName == null) {
        return new ByIndexOutParameterRegistration(outParameterSqlIndex, outParameterType);
      } else {
        return new ByIndexAndTypeNameOutParameterRegistration(outParameterSqlIndex, outParameterType, returnTypeName);
      }
    }

    private static String getOutParameterName(Method method) {
      OutParameter outParameter = method.getAnnotation(OutParameter.class);
      ReturnValue returnValue = method.getAnnotation(ReturnValue.class);
      String outParameterName;
      if (outParameter != null) {
        if (returnValue != null) {
          throw new IllegalArgumentException("method " + method + " needs to be annotated with only one of" + OutParameter.class + " or " + ReturnValue.class);
        }
        outParameterName = outParameter.name();
      } else if (returnValue != null) {
        outParameterName = returnValue.name();
      } else {
        outParameterName = null;
      }
      // correct annotation default values
      if (outParameterName != null && outParameterName.isEmpty()) {
        return null;
      }
      return outParameterName;
    }

    private static int getOutParameterSqlIndex(Method method) {
      OutParameter outParameter = method.getAnnotation(OutParameter.class);
      InOutParameter inOutParameter = method.getAnnotation(InOutParameter.class);
      ReturnValue returnValue = method.getAnnotation(ReturnValue.class);
      if (countNonNulls(outParameter, inOutParameter, returnValue) > 1) {
        throw new IllegalArgumentException("method " + method + " needs to be annotated with only one of "
                + OutParameter.class + ", " + InOutParameter.class + " or " + ReturnValue.class);
      }
      int outParameterIndex;
      if (outParameter != null) {
        outParameterIndex = outParameter.index();
      } else if (inOutParameter != null) {
          outParameterIndex = inOutParameter.index();
      } else if (returnValue != null) {
        // always the first parameter
        outParameterIndex = 1;
      } else {
        // we will use a ResultSet instead of an out parameter or function return value
        // eg for Mysql or H2
        outParameterIndex = NO_OUT_PARAMTER;
      }
      if (outParameterIndex == NO_OUT_PARAMTER) {
        if (outParameter != null || returnValue != null) {
          // default for the out parameter index is the last index
          // but only if we use an out parameter for function return value
          // if we use a result set then we have no out parameter
          return getInputParameterCount(method) + 1;
        } else if (inOutParameter != null) {
          // default for the inout parameter index is the last index
          return getInputParameterCount(method);
        }
      }
      return outParameterIndex;
    }

    private static int countNonNulls(Object o1, Object o2, Object o3) {
      int count = 0;
      if (o1 != null) {
        count += 1;
      }
      if (o2 != null) {
        count += 1;
      }
      if (o3 != null) {
        count += 1;
      }
      return count;
    }

    private static String getReturnTypeName(Method method) {
      OutParameter outParameter = method.getAnnotation(OutParameter.class);
      ReturnValue returnValue = method.getAnnotation(ReturnValue.class);
      String typeName;
      if (outParameter != null) {
        typeName = outParameter.typeName();
      } else if (returnValue != null) {
        typeName = returnValue.typeName();
      } else {
        typeName = ""; // default
      }
      if ("".equals(typeName)) {
        return null;
      }
      return typeName;
    }

    private int getOutParameterType(Method method) {
      Class<?> methodReturnType = method.getReturnType();
      if (methodReturnType == void.class) {
        return NO_OUT_PARAMTER;
      }
      OutParameter outParameter = method.getAnnotation(OutParameter.class);
      ReturnValue returnValue = method.getAnnotation(ReturnValue.class);
      int outParameterType;
      if (outParameter != null) {
        if (returnValue != null) {
          throw new IllegalArgumentException("method " + method + " needs to be annotated with only one of" + OutParameter.class + " or " + ReturnValue.class);
        }
        outParameterType = outParameter.type();
      } else if (returnValue != null) {
        outParameterType = returnValue.type();
      } else {
        outParameterType = Integer.MIN_VALUE;
      }
      if (outParameterType == Integer.MIN_VALUE) {
        if (methodReturnType == List.class) {
          return Types.REF_CURSOR;
        } else {
          return this.typeMapper.mapToSqlType(methodReturnType);
        }
      }
      return outParameterType;
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
        ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
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

    static byte[] buildInParameterIndices(int parameterCount, Class<?>[] methodParameterTypes) {
      byte[] indices = new byte[parameterCount];
      for (int i = 0; i < indices.length; i++) {
        if (ValueExtractorUtils.isAnyValueExtractor(methodParameterTypes[i])) {
          indices[i] = NO_IN_PARAMTER;
          continue;
        }
        indices[i] = ByteUtils.toByte(i + 1);
      }
      return indices;
    }

    static byte[] buildInParameterIndices(int parameterCount, int outParameterIndex, Class<?>[] methodParameterTypes) {
      byte[] indices = new byte[parameterCount];
      for (int i = 0; i < indices.length; i++) {
        if (ValueExtractorUtils.isAnyValueExtractor(methodParameterTypes[i])) {
          indices[i] = NO_IN_PARAMTER;
          continue;
        }
        if (outParameterIndex > i + 1) {
          indices[i] = ByteUtils.toByte(i + 1);
        } else {
          indices[i] = ByteUtils.toByte(i + 2);
        }
      }
      return indices;
    }

    private static int getValueExtractorIndex(Method method) {
      Class<?>[] methodParameterTypes = method.getParameterTypes();
      for (int i = 0; i < methodParameterTypes.length; i++) {
        Class<?> methodParameterType = methodParameterTypes[i];
        boolean valueExtractor = ValueExtractorUtils.isValueExtractor(methodParameterType);
        boolean numberedValueExtractor = ValueExtractorUtils.isNumberedValueExtractor(methodParameterType);
        if (valueExtractor && numberedValueExtractor) {
          throw new IllegalArgumentException(methodParameterType + " is both: " + ValueExtractor.class
                  + " and " + NumberedValueExtractor.class + " but should only be one");
        }
        if (valueExtractor || numberedValueExtractor) {
          return i;
        }
      }
      return NO_VALUE_EXTRACTOR;
    }

    private static int getInputParameterCount(Method method) {
      int count = 0;
      for (Class<?> parameterType : method.getParameterTypes()) {
        if (!ValueExtractorUtils.isAnyValueExtractor(parameterType)) {
          count += 1;
        }
      }
      return count;
    }

    private String buildCallString(Method method, String procedureName,
            int sqlInputParameterCount, boolean hasOutParameter) {
      String namespace = this.hasNamespace(method) ? this.extractsNamespace(method) : null;
      String schemaName = this.hasSchema(method) ? this.extractSchema(method) : null;
      boolean isFunction = procedureHasReturnValue(method);
      if (isFunction) {
        return buildQualifiedFunctionCallString(namespace, schemaName, procedureName, sqlInputParameterCount);
      } else {
        int sqlParameterCount;
        if (hasOutParameter && !shareOutParameter(method)) {
          sqlParameterCount = sqlInputParameterCount + 1;
        } else {
          sqlParameterCount = sqlInputParameterCount;
        }
        return buildQualifiedProcedureCallString(namespace, schemaName, procedureName, sqlParameterCount);
      }
    }

    private static boolean shareOutParameter(Method method) {
      return method.isAnnotationPresent(InOutParameter.class);
    }

    static String buildQualifiedProcedureCallString(String namespace, String schemaName, String functionName, int parameterCount) {
      // {call RAISE_PRICE(?,?,?)}
      return buildCallString("{call ", namespace, schemaName, functionName, parameterCount);
    }

    static String buildQualifiedFunctionCallString(String namespace, String schemaName, String functionName, int parameterCount) {
      // { ? = call RAISE_PRICE(?,?,?)}
      return buildCallString("{ ? = call ", namespace, schemaName, functionName, parameterCount);
    }

    static String buildCallString(String prefix, String namespace, String schemaName, String functionName, int parameterCount) {
      // compute the capacity
      int capacity = prefix.length(); // { ? = call
      if (namespace != null) {
        capacity += namespace.length()
                + 1; // .
      }
      if (schemaName != null) {
        capacity += schemaName.length()
                + 1; // .
      }
      capacity += functionName.length()
              + 1 // (
              + Math.max(parameterCount * 2 - 1, 0) // ?,?
              + 2; // )}

      // build the string
      StringBuilder builder = new StringBuilder(capacity);
      builder.append(prefix);
      if (namespace != null) {
        builder.append(namespace);
        builder.append('.');
      }
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

    private boolean hasSchema(Method method) {
      return this.hasSchema || method.getDeclaringClass().isAnnotationPresent(Schema.class);
    }

    private String extractSchema(Method method) {
      Class<?> declaringClass = method.getDeclaringClass();
      Schema schemaAnnotation = declaringClass.getAnnotation(Schema.class);
      if (schemaAnnotation != null) {
        String schema = schemaAnnotation.value();
        if (!schema.isEmpty()) {
          return schema;
        }
      }
      return this.schemaNamingStrategy.translateToDatabase(declaringClass.getSimpleName());
    }

    private boolean hasNamespace(Method method) {
      return this.hasNamespace || method.getDeclaringClass().isAnnotationPresent(Schema.class);
    }

    private String extractsNamespace(Method method) {
      Class<?> declaringClass = method.getDeclaringClass();
      Namespace namespaceAnnotation = declaringClass.getAnnotation(Namespace.class);
      if (namespaceAnnotation != null) {
        String namespace = namespaceAnnotation.value();
        if (!namespace.isEmpty()) {
          return namespace;
        }
      }
      return this.namespaceNamingStrategy.translateToDatabase(declaringClass.getSimpleName());
    }

  }

  /**
   * Information about how to call a stored procedure.
   *
   * <p>For every method in an interface there is a lazily creates instance of
   * this class.</p>
   *
   * <p>Makes heavy use of polymorphism to only store the absolute
   * minimum information (at the cost of additional objects).</p>
   *
   * <p>This class is immutable.</p>
   */
  static final class CallInfo {

    final String procedureName;
    final String callString;
    final boolean wantsExceptionTranslation;
    final ResultExtractor resultExtractor;
    final OutParameterRegistration outParameterRegistration;
    final InParameterRegistration inParameterRegistration;
    final CallResourceFactory callResourceFactory;

    CallInfo(String procedureName, String callString, boolean wantsExceptionTranslation,
            ResultExtractor resultExtractor, OutParameterRegistration outParameterRegistration,
            InParameterRegistration inParameterRegistration, CallResourceFactory callResourceFactory) {
      this.procedureName = procedureName;
      this.callString = callString;
      this.wantsExceptionTranslation = wantsExceptionTranslation;
      this.resultExtractor = resultExtractor;
      this.outParameterRegistration = outParameterRegistration;
      this.inParameterRegistration = inParameterRegistration;
      this.callResourceFactory = callResourceFactory;
    }

    @Override
    public String toString() {
      return "call '" + this.procedureName + "' using call string \"" + this.callString + '\"'
              + (this.wantsExceptionTranslation ? " with exception translation" : " without exception translation")
              + ", resultExtractor: " + this.resultExtractor
              + ", outParameterRegistration: " + this.outParameterRegistration
              + ", inParameterRegistration: " + this.inParameterRegistration
              + ", callResourceFactory: " + this.callResourceFactory;
    }

  }

}
