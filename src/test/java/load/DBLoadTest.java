package load;

import dao.note.JpaNoteDao;
import dao.user.JpaUserDao;
import dao.notebook.JpaNotebookDao;
import datasource.MariaDbJpaConnection;
import entity.entities.NoteEntity;
import entity.entities.NotebookEntity;
import entity.entities.UserEntity;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DBLoadTest {

    private static final String CSV_FILE = "load-testing/load-test-results.csv";
    private static final Logger logger = LoggerFactory.getLogger(DBLoadTest.class);

    private static final int THREADS = 20;
    private static final int ITERATIONS = 50;
    private static final int WARMUP = 20;
    private static final int TIMEOUT_SECONDS = 300;

    private static final JpaUserDao userDao = new JpaUserDao();
    private static final JpaNoteDao noteDao = new JpaNoteDao();
    private static final JpaNotebookDao notebookDao = new JpaNotebookDao();

    private static Long notebookId;

    private static final ConcurrentLinkedQueue<Throwable> errors = new ConcurrentLinkedQueue<>();
    private static final List<Long> notePool = new CopyOnWriteArrayList<>();

    private static final ConcurrentLinkedQueue<Long> latencies = new ConcurrentLinkedQueue<>();
    private static final AtomicInteger failures = new AtomicInteger();
    private static final AtomicInteger completedOps = new AtomicInteger();

    private static final int NOTE_POOL_SIZE = 100;
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_BACKOFF_MS = 50;

    @BeforeAll
    static void setup() {
        String unique = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        UserEntity user = new UserEntity("Load", "Tester", "load_user_" + unique, "load+" + unique + "@test.local");
        user = userDao.save(user);

        NotebookEntity notebook = new NotebookEntity(user);
        user.addNotebook(notebook);

        // Persist the notebook explicitly and capture the managed instance so we have its id.
        notebook = notebookDao.save(notebook);
        notebookId = notebook.getId();

        // Warm-up phase (avoids cold start bias) — fetch per-iteration to avoid sharing managed entity across threads
        for (int i = 0; i < WARMUP; i++) {
            notebookDao.findById(notebookId);
            noteDao.findByNotebookWithTranslations(notebookDao.findById(notebookId));
        }

        // Pre-create a pool of notes with an English translation to avoid concurrent translation INSERT races
        for (int i = 0; i < NOTE_POOL_SIZE; i++) {
            NoteEntity n = new NoteEntity();
            n.setNotebook(notebookDao.findById(notebookId));
            n.createTranslation("EN").setTitle("load-pool-" + i + "-" + System.nanoTime());
            n = noteDao.save(n);
            notePool.add(n.getId());
        }
    }

    @AfterAll
    static void cleanup() {
        MariaDbJpaConnection.shutdown();
    }

    @Test
    void stressTest() throws InterruptedException {

        CountDownLatch ready = new CountDownLatch(THREADS);
        CountDownLatch start = new CountDownLatch(1);

        // Use a small AutoCloseable wrapper so we can use try-with-resources for proper shutdown handling of the ExecutorService
        Instant startTime = Instant.now();

        try (ExecutorCloser closer = new ExecutorCloser(Executors.newFixedThreadPool(THREADS))) {
            ExecutorService executor = closer.executor;

            for (int t = 0; t < THREADS; t++) {
                executor.submit(() -> {
                    ready.countDown();

                    try {
                        start.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return null;
                    }

                    for (int i = 0; i < ITERATIONS; i++) {
                        int op = ThreadLocalRandom.current().nextInt(100);

                        if (op < 40) {
                            timed(this::createNote);
                        } else if (op < 70) {
                            timed(this::readNotes);
                        } else if (op < 90) {
                            timed(this::updateNote);
                        } else {
                            timed(this::createAndDelete);
                        }
                    }

                    return null;
                });
            }

            ready.await();
            start.countDown();

            // closure's close() will attempt an orderly shutdown and then a forced shutdown if necessary
        }

        Instant endTime = Instant.now();

        List<Long> snapshot = new ArrayList<>(latencies);
        snapshot.sort(Long::compareTo);

        int size = snapshot.size();

        long min = size == 0 ? 0 : Collections.min(snapshot);
        long max = size == 0 ? 0 : Collections.max(snapshot);

        double avg = snapshot.stream().mapToLong(Long::longValue).average().orElse(0);

        int p95Index = size == 0 ? 0 : (int) Math.ceil(size * 0.95) - 1;
        p95Index = Math.max(0, Math.min(p95Index, size - 1));
        long p95 = size == 0 ? 0 : snapshot.get(p95Index);

        long totalOps = THREADS * ITERATIONS;
        long completed = completedOps.get();

        double seconds = Math.max(0.001, Duration.between(startTime, endTime).toMillis() / 1000.0);

        double throughput = completed / seconds;

        // Don't remove this printout.
        System.out.println("===== LOAD TEST RESULTS =====");
        System.out.println("Ops: " + totalOps);
        System.out.println("Completed: " + completed);
        System.out.println("Failures: " + failures.get());
        System.out.println("Avg ms: " + avg);
        System.out.println("Min ms: " + min);
        System.out.println("Max ms: " + max);
        System.out.println("P95 ms: " + p95);
        System.out.printf("Throughput ops/sec: %.2f", throughput);
        System.out.println();

        // Export to CSV
        try (PrintWriter writer = new PrintWriter(CSV_FILE)) {
            writer.println("index,latency_ms");

            int i = 0;
            for (Long latency : latencies) {
                writer.println((i++) + "," + latency);
            }

            System.out.println("CSV exported: " + CSV_FILE);

        } catch (Exception e) {
            logger.error("Failed to export CSV", e);
        }

        // Print brief error/exception summary for diagnostics
        if (!errors.isEmpty()) {
            System.out.println("===== ERROR SUMMARY =====");

            // Group by exception class and message to provide more context
            Map<String, Integer> counts = new HashMap<>();
            for (Throwable t : errors) {
                String key = t.getClass().getSimpleName() + ": " + (t.getMessage() == null ? "<no-message>" : t.getMessage());
                counts.merge(key, 1, Integer::sum);
            }

            counts.forEach((k, v) -> System.out.println(v + " x " + k));

            // Print up to first 10 full stack traces for deeper diagnostics
            System.out.println("\n===== SAMPLE STACK TRACES (up to 10) =====");
            int printed = 0;
            for (Throwable t : errors) {
                if (printed++ >= 10) break;
                System.out.println("--- Exception #" + printed + " ---");
                t.printStackTrace(System.out);
            }
        }


        // Basic assertions so test fails when there are excessive failures or no completed operations
        assertTrue(completed > 0, "No completed operations recorded");
        assertTrue(failures.get() <= (totalOps * 0.1), "Too many failures: " + failures.get());
    }

    private void timed(Runnable r) {
        long start = System.nanoTime();
        boolean success = false;
        Throwable last = null;

        try {
            int attempt = 1;
            while (true) {
                try {
                    r.run();
                    success = true;
                    completedOps.incrementAndGet();
                    break;
                } catch (Exception e) {
                    last = e;

                    if (isRetryable(e) && attempt < MAX_RETRIES) {
                        long nanos = java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(RETRY_BACKOFF_MS * attempt);
                        java.util.concurrent.locks.LockSupport.parkNanos(nanos);

                        if (Thread.currentThread().isInterrupted()) {
                            last = new InterruptedException("Interrupted during backoff");
                            Thread.currentThread().interrupt();
                            break;
                        }

                        attempt++;
                        continue;
                    }
                    break;
                }
            }
        } finally {
            long end = System.nanoTime();
            latencies.add((end - start) / 1_000_000);

            if (!success) {
                failures.incrementAndGet();
                if (last != null) errors.add(last);
            }
        }
    }

    private boolean isRetryable(Throwable e) {
        if (e == null) return false;
        // check common JPA/Hibernate exceptions indicating concurrent modification or transient commit failure
        Throwable cur = e;
        while (cur != null) {
            String cls = cur.getClass().getName();
            if (cur instanceof jakarta.persistence.OptimisticLockException) return true;
            if (cur instanceof jakarta.persistence.RollbackException) return true;
            if (cls.equals("org.hibernate.StaleObjectStateException")) return true;
            if (cls.equals("org.hibernate.exception.LockAcquisitionException")) return true;

            if (cur instanceof org.hibernate.exception.ConstraintViolationException) {
                return false;
            }

            cur = cur.getCause();
        }
        return false;
    }

    private void createNote() {
        NoteEntity n = new NoteEntity();
        n.setNotebook(getNotebook());

        n.createTranslation("en").setTitle("load-" + Thread.currentThread().getName() + "-" + System.nanoTime());

        noteDao.save(n);
    }

    private void readNotes() {
        noteDao.findByNotebookWithTranslations(getNotebook());
    }

    private void updateNote() {
        if (notePool.isEmpty()) return;

        // Pick a random note from the pre-created pool to reduce contention
        Long noteId = notePool.get(ThreadLocalRandom.current().nextInt(notePool.size()));
        NoteEntity n = noteDao.findById(noteId);
        if (n == null) return;

        n.createTranslation("en").setTitle("updated-" + System.nanoTime());
        noteDao.update(n);
    }

    private void createAndDelete() {
        NoteEntity n = new NoteEntity();
        n.setNotebook(getNotebook());
        n.createTranslation("en").setTitle("temporary");

        n = noteDao.save(n);
        noteDao.delete(n);
    }

    private NotebookEntity getNotebook() {
        if (notebookId == null) return null;
        return notebookDao.findById(notebookId);
    }

    //  Helper to allow try-with-resources for ExecutorService shutdown handling
    private static final class ExecutorCloser implements AutoCloseable {
        final ExecutorService executor;

        ExecutorCloser(ExecutorService executor) {
            this.executor = executor;
        }

        @Override
        public void close() {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}