package datasource;

/**
 * Custom exception for database configuration issues.
 */
class DatabaseConfigurationException extends RuntimeException {
    public DatabaseConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
