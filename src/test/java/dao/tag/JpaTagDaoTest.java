package dao.tag;

import datasource.MariaDbJpaConnection;
import entity.base.BaseEntity;
import entity.entities.NotebookEntity;
import entity.entities.NoteEntity;
import entity.entities.TagEntity;
import entity.entities.UserEntity;
import jakarta.persistence.EntityManager;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JpaTagDaoTest {

    private static JpaTagDao tagDao;
    private EntityManager em;

    @BeforeAll
    static void setupAll() {
        tagDao = new JpaTagDao();
    }

    @BeforeEach
    void setUp() {
        em = MariaDbJpaConnection.getEntityManager();
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
        }
    }

    @AfterEach
    void tearDown() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        em.clear();
    }

    private String uniqueName(String base) {
        String shortId = UUID.randomUUID().toString().substring(0, 4);
        return base + "_" + shortId;
    }

    @Test
    void testSaveAndFindTag() {
        TagEntity tag = new TagEntity();
        tag.setTagName(uniqueName("TestTag"));
        TagEntity saved = tagDao.save(tag);

        em.flush();

        TagEntity found = tagDao.findById(saved.getId());
        assertNotNull(found);
        assertEquals(saved.getTagName(), found.getTagName());
    }

    @Test
    void testSaveWithExistingIdMerges() {
        String name = uniqueName("MergeTag");
        TagEntity tag = new TagEntity();
        tag.setTagName(name);
        tag = tagDao.save(tag);

        tag.setTagName(uniqueName("MergedName"));

        TagEntity merged = tagDao.save(tag); // for merge branch

        assertEquals(tag.getId(), merged.getId());
        assertEquals(tag.getTagName(), merged.getTagName());
    }

    @Test
    void testFindByIdNotFound() {
        assertThrows(RuntimeException.class, () -> tagDao.findById(99999L));
    }

    @Test
    void testExistsByName() {
        String name = uniqueName("ExistsTag");
        TagEntity tag = new TagEntity();
        tag.setTagName(name);
        tagDao.save(tag);

        em.flush();

        assertTrue(tagDao.existsByName(name));
        assertFalse(tagDao.existsByName(name + "_none"));
    }

    @Test
    void testFindByName() {
        String name = uniqueName("FindByName");
        TagEntity tag = new TagEntity();
        tag.setTagName(name);
        tagDao.save(tag);

        em.flush();

        TagEntity found = tagDao.findByName(name);
        assertNotNull(found);
        assertEquals(name, found.getTagName());

        assertNull(tagDao.findByName("NonExistent"));
    }

    @Test
    void testFindByNameNotFound() {
        assertNull(tagDao.findByName("NoSuchTag"));
    }

    @Test
    void testFindAll() {
        int initialSize = tagDao.findAll().size();

        TagEntity tag1 = new TagEntity();
        tag1.setTagName(uniqueName("Tag1"));
        tagDao.save(tag1);

        TagEntity tag2 = new TagEntity();
        tag2.setTagName(uniqueName("Tag2"));
        tagDao.save(tag2);

        em.flush();

        List<TagEntity> allTags = tagDao.findAll();
        assertTrue(allTags.contains(tag1));
        assertTrue(allTags.contains(tag2));
        assertTrue(allTags.size() >= initialSize + 2);
    }

    @Test
    void testFindAllEmpty() {
        String name = uniqueName("CheckEmpty");
        TagEntity tag = new TagEntity();
        tag.setTagName(name);
        tagDao.save(tag);
        em.flush();

        tagDao.delete(tag);
        em.flush();

        assertNull(tagDao.findByName(name));
    }

    @Test
    void testUpdateTag() {
        String name = uniqueName("OldName");
        TagEntity tag = new TagEntity();
        tag.setTagName(name);
        tag = tagDao.save(tag);

        tag.setTagName(uniqueName("NewName"));
        tagDao.update(tag);

        em.flush();

        TagEntity updated = tagDao.findById(tag.getId());
        assertEquals(tag.getTagName(), updated.getTagName());
    }

    @Test
    void testUpdateDuplicateDoesNotChangeOriginal() {
        String name1 = uniqueName("TagA");
        String name2 = uniqueName("TagB");

        // Create and persist the original tag that will cause the duplicate
        TagEntity tag1 = new TagEntity();
        tag1.setTagName(name1);
        tagDao.save(tag1);

        TagEntity tag2 = new TagEntity();
        tag2.setTagName(name2);
        tagDao.save(tag2);

        em.flush();

        // Attempt to rename tag2 to name1 should fail
        tag2.setTagName(name1);

        try {
            tagDao.update(tag2);
        } catch (RuntimeException e) {
            // If update() already flushed and failed, we caught it.
        }

        assertThrows(RuntimeException.class, () -> em.flush());

        em.clear();

        // Ensure original tag2 name unchanged in DB
        TagEntity reloaded = tagDao.findById(tag2.getId());
        assertEquals(name2, reloaded.getTagName());
    }

    @Test
    void testDeleteTag() {
        TagEntity tag = new TagEntity();
        tag.setTagName(uniqueName("ToDelete"));
        tagDao.save(tag);

        // associate a note to ensure deletion handles removal
        NoteEntity note = new NoteEntity();

        var translation = note.createTranslation("EN");
        translation.setTitle("Title");
        translation.setContent("Content");
        translation.setAnnotation("Annotation");

        note.addTag(tag);

        tagDao.delete(tag);

        long id = tag.getId();
        assertThrows(RuntimeException.class, () -> tagDao.findById(id));
    }

    @Test
    void testDeleteTagWithRealNoteAssociation() {
        TagEntity tag = new TagEntity();
        tag.setTagName(uniqueName("TagNote"));
        tagDao.save(tag);

        UserEntity user = new UserEntity();
        user.setFirstName("Test");
        user.setLastName("User");
        user.setUsername("tester_" + System.currentTimeMillis());
        user.setEmail("test_" + System.currentTimeMillis() + "@example.com");
        user.changePasswordHash("Test@123");
        em.persist(user);

        NotebookEntity notebook = new NotebookEntity(user);
        em.persist(notebook);

        NoteEntity note = new NoteEntity();
        note.setNotebook(notebook);
        note.addTag(tag);

        var translation = note.createTranslation("en");
        translation.setTitle("Title");
        translation.setContent("Content");
        translation.setAnnotation("Annotation");

        em.persist(note);
        em.flush();

        tagDao.delete(tag);

        Long id = tag.getId();
        assertThrows(RuntimeException.class, () -> tagDao.findById(id));
    }

    @Test
    void testDeleteNonExistentTag() {
        TagEntity tag = new TagEntity();
        tag.setTagName("NonExistent");
        setId(tag, 99999L); // reflection to set fake ID
        assertThrows(RuntimeException.class, () -> tagDao.delete(tag));
    }

    @Test
    void testSaveDuplicateThrows() {
        String name = uniqueName("DumpsTag");
        TagEntity tag1 = new TagEntity();
        tag1.setTagName(name);
        tagDao.save(tag1);

        TagEntity tag2 = new TagEntity();
        tag2.setTagName(name);

        assertThrows(IllegalArgumentException.class, () -> tagDao.save(tag2));
    }

    @Test
    void testUpdateDuplicateThrows() {
        String name1 = uniqueName("Tag1");
        String name2 = uniqueName("Tag2");

        TagEntity tag1 = new TagEntity();
        tag1.setTagName(name1);
        tagDao.save(tag1);

        TagEntity tag2 = new TagEntity();
        tag2.setTagName(name2);
        tagDao.save(tag2);

        em.flush();

        // try to rename tag2 to tag1's name
        tag2.setTagName(name1);
        assertThrows(ConstraintViolationException.class, () -> tagDao.update(tag2)); // or tagDao.update(tag2)
    }

    @Test
    void testDeleteNullTagThrows() {
        assertThrows(IllegalArgumentException.class, () -> tagDao.delete(null));
    }

    @Test
    void testUpdateNullTagThrows() {
        assertThrows(IllegalArgumentException.class, () -> tagDao.update(null));
    }

    @Test
    void testSaveNullTagThrows() {
        assertThrows(IllegalArgumentException.class, () -> tagDao.save(null));
    }

    private void setId(BaseEntity entity, Long id) {
        try {
            Field idField = BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
