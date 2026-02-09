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
                properties.put("jakarta.persistence.jdbc.user", System.getenv("DB_USER"));
                properties.put("jakarta.persistence.jdbc.password", System.getenv("DB_PASSWORD"));

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
