package com.github.marschall.storedprocedureproxy.spi;

import java.util.Objects;

/**
 * Derives a database name of an object from the Java name of an object.
 *
 * <p>Provides various convenience methods for chaining several
 * implementations. For example if the Java name of your stored procedure
 * is {@code "blitz"} but the SQL name is {@code "sp_Blitz"} then you can
 * create this transformation using:</p>
 * <pre><code>
 * NamingStrategy.capitalize() // converts "blitz" to "Blitz"
 *    .thenPrefix("sp_") // converts "Blitz" to "sp_Blitz"
 * </code></pre>
 *
 */
@FunctionalInterface
public interface NamingStrategy {

  /**
   * The identity transformation. Simply returns the argument unchanged.
   */
  public static NamingStrategy IDENTITY = (s) -> s;

  /**
   * Derives a database name of an object from the Java name of an object.
   *
   * @param javaName the Java name of an object, never {@code null}
   * @return the database name of an object, never {@code null}
   */
  String translateToDatabase(String javaName);

  /**
   * Creates a new transformation that converts the entire string to upper case.
   *
   * <p>Only works reliably for characters from the US-ASCII latin alphabet.</p>
   *
   * @return a new transformation that converts the entire string to upper case
   */
  public static NamingStrategy upperCase() {
    return UpperCase.INSTANCE;
  }

  /**
   * Creates a new transformation that converts the entire string to lower case.
   *
   * <p>Only works reliably for characters from the US-ASCII latin alphabet.</p>
   *
   * @return a new transformation that converts the entire string to lower case
   */
  public static NamingStrategy lowerCase() {
    return LowerCase.INSTANCE;
  }

  /**
   * Creates a new transformation that converts the first character to
   * upper case.
   *
   * <p>Only works for characters from the US-ASCII latin alphabet.</p>
   *
   * @return a new transformation that converts the first character to upper case
   */
  public static NamingStrategy capitalize() {
    return Capitalize.INSTANCE;
  }

  /**
   * Creates a new transformation that converts the entire string to
   * <a href="https://en.wikipedia.org/wiki/Snake_case">snake case</a>.
   *
   * <p>For example turns {@code "procedureName"} into {@code "procedure_Name"}.
   * No case conversion is done so you'll likely want to combine this with
   * either {@link #thenUpperCase()} or {@link #thenLowerCase()}.</p>
   *
   * @return a new transformation that converts the entire string to snake case
   */
  public static NamingStrategy snakeCase() {
    return SnakeCase.INSTANCE;
  }

  /**
   * Creates a new transformation that applies a prefix to the string.
   *
   * @param prefix the prefix to append, not {@code null}
   * @return a new transformation that applies a prefix
   */
  public static NamingStrategy prefix(String prefix) {
    Objects.requireNonNull(prefix);
    return new Prefix(prefix);
  }

  /**
   * Creates a new transformation that skips a given number of characters
   * from the start of the java name.
   *
   * @param skipped the number of characters from the start
   * @return a new transformation that skips the first characters
   */
  public static NamingStrategy withoutFirst(int skipped) {
    return new WithoutFirst(skipped);
  }

  /**
   * Applies another transformation after the current transformation.
   *
   * @param next the transformation to apply after the current one, not {@code null}
   * @return a new transformation that applies the given transformation after the current transformation
   */
  default NamingStrategy then(NamingStrategy next) {
    Objects.requireNonNull(next);
    return new Compund(this, next);
  }

  /**
   * Applies a upper case transformation of the entire string after the current transformation.
   *
   * <p>Only works reliably for characters from the US-ASCII latin alphabet.</p>
   *
   * @return a new transformation that applies a upper case transformation after the current transformation
   */
  default NamingStrategy thenUpperCase() {
    return then(upperCase());
  }

  /**
   * Applies a lower case transformation of the entire string after the current transformation.
   *
   * <p>Only works reliably for characters from the US-ASCII latin alphabet.</p>
   *
   * @return a new transformation that applies a lower case transformation after the current transformation
   */
  default NamingStrategy thenLowerCase() {
    return then(lowerCase());
  }

  /**
   * Applies an upper case transformation of the first character after the
   * current transformation.
   *
   * <p>Only works for characters from the US-ASCII latin alphabet.</p>
   *
   * @return a new transformation that applies captialisation of the first
   *         character after the current transformation
   */
  default NamingStrategy thenCapitalize() {
    return then(capitalize());
  }

  /**
   * Applies <a href="https://en.wikipedia.org/wiki/Snake_case">snake case</a>
   * after the current transformation.
   *
   * <p>For example turns {@code "procedureName"} into {@code "procedure_Name"}.
   * No case conversion is done so you'll likely want to combine this with
   * either {@link #thenUpperCase()} or {@link #thenLowerCase()}.</p>
   *
   * @return a new transformation that applies snake case after the current transformation
   */
  default NamingStrategy thenSnakeCase() {
    return then(snakeCase());
  }

  /**
   * Appends a prefix after the current transformation.
   *
   * @param prefix the prefix to append, not {@code null}
   * @return a new transformation that applies a prefix after the current transformation
   */
  default NamingStrategy thenPrefix(String prefix) {
    Objects.requireNonNull(prefix);
    return then(prefix(prefix));
  }

  /**
   * Skips a number of characters from the start of the name after the current
   * transformation.
   *
   * @param skipped the number of characters from the start
   * @return a new transformation that skips {@code skipped} characters after the current transformation
   */
  default NamingStrategy thenWithoutFirst(int skipped) {
    return then(withoutFirst(skipped));
  }

}
