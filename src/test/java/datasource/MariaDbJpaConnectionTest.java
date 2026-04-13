package datasource;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MariaDbJpaConnectionTest {

    private EntityManagerFactory mockEmf;

    @BeforeEach
    void setup() {
        mockEmf = mock(EntityManagerFactory.class);
        EntityManager mockEm = mock(EntityManager.class);
        when(mockEmf.createEntityManager()).thenReturn(mockEm);
        when(mockEmf.isOpen()).thenReturn(true);

        // Inject mocks using targeted reflection
        injectStaticField("emf", mockEmf);
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
            Field field = MariaDbJpaConnection.class.getDeclaredField("emf");
            field.setAccessible(true);
            return (EntityManagerFactory) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Failed to read 'emf' field via reflection", e);
        }
    }

    private void injectStaticField(String fieldName, Object value) {
        try {
            Field field = MariaDbJpaConnection.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(null, value);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Field '" + fieldName + "' not found in MariaDbJpaConnection. Did the production code change?", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Security Manager prevented access to field: " + fieldName, e);
        }
    }
}
