package datasource;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MariaDbJpaConnectionTest {

    private EntityManagerFactory mockEmf;
    private EntityManager mockEm;

    @BeforeEach
    void setup() {
        mockEmf = mock(EntityManagerFactory.class);
        mockEm = mock(EntityManager.class);
        when(mockEmf.createEntityManager()).thenReturn(mockEm);
        when(mockEmf.isOpen()).thenReturn(true);

        // Inject mocks using reflection
        try {
            var emfField = MariaDbJpaConnection.class.getDeclaredField("emf");
            emfField.setAccessible(true);
            emfField.set(null, mockEmf);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testCreateEntityManagerNotNull() {
        EntityManager em = MariaDbJpaConnection.createEntityManager();
        assertNotNull(em);
        verify(mockEmf, times(1)).createEntityManager();
    }

    @Test
    void testShutdownClosesFactory() {
        MariaDbJpaConnection.shutdown();
        assertNull(getStaticEmfField());
    }

    private EntityManagerFactory getStaticEmfField() {
        try {
            var emfField = MariaDbJpaConnection.class.getDeclaredField("emf");
            emfField.setAccessible(true);
            return (EntityManagerFactory) emfField.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
