package datasource;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class MariaDbJpaConnection {

    private static EntityManagerFactory emf = null;

    private static synchronized void ensureFactory() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("CompanyMariaDbUnit");
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
