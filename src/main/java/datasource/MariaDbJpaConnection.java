package datasource;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MariaDbJpaConnection {

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
        if (emf == null) {
            try {
                Map<String, String> properties = new ConcurrentHashMap<>();

                String dbUser = System.getProperty("DB_USER");
                if (dbUser == null) dbUser = System.getenv("DB_USER");

                String dbPassword = System.getProperty("DB_PASSWORD");
                if (dbPassword == null) dbPassword = System.getenv("DB_PASSWORD");

                String dbHost = System.getProperty("DB_HOST");
                if (dbHost == null) dbHost = System.getenv("DB_HOST");

                String dbPort = System.getProperty("DB_PORT");
                if (dbPort == null) dbPort = System.getenv("DB_PORT");

                String dbName = System.getProperty("DB_NAME");
                if (dbName == null) dbName = System.getenv("DB_NAME");

                boolean missing = dbUser == null || dbPassword == null || dbHost == null || dbPort == null || dbName == null
                        || isBlank(dbUser) || isBlank(dbPassword) || isBlank(dbHost) || isBlank(dbPort) || isBlank(dbName);

                if (!missing) {
                    // Use MariaDB as configured
                    LOGGER.info("Using MariaDB DB connection");

                    // Request utf8mb4 end-to-end (4-byte Unicode) from the driver
                    String jdbcUrl = "jdbc:mariadb://" + dbHost + ":" + dbPort + "/" + dbName
                            + "?useUnicode=true&characterEncoding=utf8mb4&connectionCollation=utf8mb4_unicode_ci";
                    properties.put("jakarta.persistence.jdbc.url", jdbcUrl);
                    properties.put("hibernate.connection.charSet", "utf8mb4");
                    properties.put("hibernate.connection.useUnicode", "true");
                    properties.put("hibernate.connection.characterEncoding", "utf8mb4");
                    properties.put("jakarta.persistence.jdbc.user", dbUser);
                    properties.put("jakarta.persistence.jdbc.password", dbPassword);
                    properties.put("jakarta.persistence.jdbc.driver", "org.mariadb.jdbc.Driver");
                    properties.put("hibernate.hbm2ddl.auto", "update");
                    properties.put("hibernate.dialect", "org.hibernate.dialect.MariaDBDialect");

                    LOGGER.info("Connecting to DB: jdbc:mariadb://{}:{}/{} with user {}", dbHost, dbPort, dbName, dbUser);
                } else {
                    // Fallback to H2 in-memory for tests and local runs without env vars
                    LOGGER.warn("Database environment variables are not set properly; falling back to in-memory H2 for tests");

                    String jdbcUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE";
                    properties.put("jakarta.persistence.jdbc.url", jdbcUrl);
                    properties.put("jakarta.persistence.jdbc.user", "sa");
                    properties.put("jakarta.persistence.jdbc.password", "");
                    properties.put("jakarta.persistence.jdbc.driver", "org.h2.Driver");

                    // Use create-drop so schema is clean for each JVM run (tests)
                    properties.put("hibernate.hbm2ddl.auto", "create-drop");
                    properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
                    properties.put("hibernate.show_sql", "false");
                }

                emf = Persistence.createEntityManagerFactory("CompanyMariaDbUnit", properties);
                LOGGER.info("EntityManagerFactory created successfully");
            } catch (Exception e) {
                LOGGER.error("Failed to initialize EntityManagerFactory. Confirm DB user and password", e);
                throw new RuntimeException("Database connection failed", e);
            }
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

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
