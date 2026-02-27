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

                properties.put("jakarta.persistence.jdbc.url",
                        "jdbc:mariadb://" + dbHost + ":" + dbPort + "/" + dbName);
                properties.put("jakarta.persistence.jdbc.user", dbUser);
                properties.put("jakarta.persistence.jdbc.password", dbPassword);

                LOGGER.info("Connecting to DB: jdbc:mariadb://{}:{}/{} with user {}", dbHost, dbPort, dbName, dbUser);
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
}
