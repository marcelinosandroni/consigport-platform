package br.com.consigport.adapters.out.persistence;

import java.sql.SQLException;

/**
 * Recognition of the unique violations of PostgreSQL.
 *
 * <p>Inside an adapter the failure of the database still carries the exception of the
 * persistence provider: the translation to the errors of the domain happens here, where
 * the constraint that was broken is known.
 */
final class UniqueViolations {

  /** The SQLSTATE of a unique violation in PostgreSQL. */
  private static final String UNIQUE_VIOLATION_SQL_STATE = "23505";

  private UniqueViolations() {}

  /**
   * Tells whether the database refused the write because of a unique constraint.
   *
   * <p>The check walks the causes, because the driver wraps the failure in the
   * exception of the persistence provider.
   *
   * @param exception the failure of the persistence provider
   * @return {@code true} when the write broke a unique constraint
   */
  static boolean isUniqueViolation(Throwable exception) {
    return findUniqueViolation(exception) != null;
  }

  /**
   * Tells whether the database refused the write because of one named constraint.
   *
   * @param exception the failure of the persistence provider
   * @param constraintName the name of the constraint, as PostgreSQL writes it
   * @return {@code true} when the write broke that constraint
   */
  static boolean isUniqueViolationOn(Throwable exception, String constraintName) {
    SQLException violation = findUniqueViolation(exception);
    return violation != null && violation.getMessage() != null
        && violation.getMessage().contains(constraintName);
  }

  private static SQLException findUniqueViolation(Throwable exception) {
    for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
      if (cause instanceof SQLException sqlException
          && UNIQUE_VIOLATION_SQL_STATE.equals(sqlException.getSQLState())) {
        return sqlException;
      }
      if (cause.getCause() == cause) {
        return null;
      }
    }
    return null;
  }
}
