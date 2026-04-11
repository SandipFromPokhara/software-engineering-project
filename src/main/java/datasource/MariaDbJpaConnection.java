package datasource;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Custom exception for database configuration issues.
 */
class DatabaseConfigurationException extends RuntimeException {
    public DatabaseConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}

public class MariaDbJpaConnection {

    private MariaDbJpaConnection() {/* Prevent instantiation */}

    private static final Logger LOGGER = LoggerFactory.getLogger(MariaDbJpaConnection.class);
    private static EntityManagerFactory emf;
    private static EntityManager sharedEm;

    public static synchronized EntityManager getEntityManager() {
        ensureFactory();
        if (sharedEm == null || !sharedEm.isOpen()) {
            sharedEm = emf.createEntityManager();
        }
        return sharedEm;
    }

    private static synchronized void ensureFactory() {
        if (emf != null) return;

        try {
            Map<String, String> properties = new ConcurrentHashMap<>();
            ConfigData config = resolveConfig();

            if (config.isValid()) {
                configureMariaDB(properties, config);
            } else {
                configureH2Fallback(properties);
            }

            emf = Persistence.createEntityManagerFactory("CompanyMariaDbUnit", properties);
            LOGGER.info("EntityManagerFactory created successfully");
        } catch (PersistenceException e) {
            throw new DatabaseConfigurationException("JPA Persistence initialization failed for 'CompanyMariaDbUnit'. Verify MariaDB reachability.", e);
        } catch (RuntimeException e) {
            throw new DatabaseConfigurationException(
                    "Unexpected configuration error for 'CompanyMariaDbUnit'.", e);
        }
    }

    private static ConfigData resolveConfig() {
        return new ConfigData(
                getEnvOrProp("DB_USER"),
                getEnvOrProp("DB_PASSWORD"),
                getEnvOrProp("DB_HOST"),
                getEnvOrProp("DB_PORT"),
                getEnvOrProp("DB_NAME")
        );
    }

    private static String getEnvOrProp(String key) {
        String val = System.getProperty(key);
        return (val != null) ? val : System.getenv(key);
    }

    private static void configureMariaDB(Map<String, String> props, ConfigData config) {
        LOGGER.info("Using MariaDB DB connection");
        String jdbcUrl = String.format("jdbc:mariadb://%s:%s/%s?useUnicode=true&characterEncoding=utf8mb4&connectionCollation=utf8mb4_unicode_ci",
                config.host, config.port, config.name);

        props.put("jakarta.persistence.jdbc.url", jdbcUrl);
        props.put("jakarta.persistence.jdbc.user", config.user);
        props.put("jakarta.persistence.jdbc.password", config.pass);
        props.put("jakarta.persistence.jdbc.driver", "org.mariadb.jdbc.Driver");
        props.put("hibernate.hbm2ddl.auto", "update");
        props.put("hibernate.dialect", "org.hibernate.dialect.MariaDBDialect");

        // Encoding properties
        props.put("hibernate.connection.charSet", "utf8mb4");
        props.put("hibernate.connection.useUnicode", "true");
        props.put("hibernate.connection.characterEncoding", "utf8mb4");
    }

    private static void configureH2Fallback(Map<String, String> props) {
        LOGGER.warn("Database environment variables are not set properly; falling back to in-memory H2");
        props.put("jakarta.persistence.jdbc.url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE");
        props.put("jakarta.persistence.jdbc.user", "sa");
        props.put("jakarta.persistence.jdbc.password", "");
        props.put("jakarta.persistence.jdbc.driver", "org.h2.Driver");
        props.put("hibernate.hbm2ddl.auto", "create-drop");
        props.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
    }

    // Simple internal record/class to hold data
    private static class ConfigData {
        String user;
        String pass;
        String host;
        String port;
        String name;

        ConfigData(String u, String p, String h, String po, String n) {
            this.user = u; this.pass = p; this.host = h; this.port = po; this.name = n;
        }
        boolean isValid() {
            return Stream.of(user, pass, host, port, name)
                    .allMatch(s -> s != null && !s.isBlank());
        }
    }

    public static EntityManager createEntityManager() {
        ensureFactory();
        return emf.createEntityManager();
    }

    public static synchronized void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
            emf = null;
        }
    }
}
