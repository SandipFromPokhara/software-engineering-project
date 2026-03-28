package dao.tag;

import datasource.MariaDbJpaConnection;
import entity.NoteBookEntity;
import entity.NoteEntity;
import entity.TagEntity;
import entity.UserEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import java.util.ArrayList;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JpaTagDaoTest {

    private static JpaTagDao tagDao;

    private List<TagEntity> testTags;

    @BeforeAll
    static void setupAll() {
        tagDao = new JpaTagDao();
    }

    private String uniqueName(String base) {
        return base + "_" + System.currentTimeMillis();
    }

    @BeforeEach
    void initTestTags() {
        testTags = new ArrayList<>();
    }

    @AfterEach
    void cleanupTestTags() {
        if (testTags != null) {
            testTags.forEach(tagDao::delete);
        }
    }

    @Test
    void testSaveAndFindTag() {
        TagEntity tag = new TagEntity(uniqueName("TestTag"));
        TagEntity saved = tagDao.save(tag);

        testTags.add(saved);

        assertNotNull(saved.getId());

        TagEntity found = tagDao.findById(saved.getId());
        assertNotNull(found);
        assertEquals(saved.getTagName(), found.getTagName());
    }

    @Test
    void testSaveWithExistingIdMerges() {
        String name = uniqueName("MergeTag");
        TagEntity tag = tagDao.save(new TagEntity(name));

        tag.setTagName(uniqueName("MergedName"));

        TagEntity merged = tagDao.save(tag); // for merge branch

        testTags.add(merged);

        assertEquals(tag.getId(), merged.getId());
        assertEquals(tag.getTagName(), merged.getTagName());
    }

    @Test
    void testFindByIdNotFound() {
        assertNull(tagDao.findById(99999L));
    }

    @Test
    void testExistsByName() {
        String name = uniqueName("ExistsTag");
        TagEntity tag = new TagEntity(name);
        tagDao.save(tag);

        testTags.add(tag);

        assertTrue(tagDao.existsByName(name));
        assertFalse(tagDao.existsByName(name + "_none"));
    }

    @Test
    void testFindByName() {
        String name = uniqueName("FindByName");
        TagEntity tag = new TagEntity(name);
        tagDao.save(tag);

        testTags.add(tag);

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
        TagEntity tag1 = tagDao.save(new TagEntity(uniqueName("Tag1")));
        TagEntity tag2 = tagDao.save(new TagEntity(uniqueName("Tag2")));

        testTags.add(tag1);
        testTags.add(tag2);

        List<TagEntity> allTags = tagDao.findAll();
        assertTrue(allTags.contains(tag1));
        assertTrue(allTags.contains(tag2));
        assertTrue(allTags.size() >= 2);
    }

    @Test
    void testFindAllEmpty() {
        tagDao.findAll().forEach(tagDao::delete); // ensure empty DB
        List<TagEntity> tags = tagDao.findAll();
        assertTrue(tags.isEmpty());
    }

    @Test
    void testUpdateTag() {
        String name = uniqueName("OldName");
        TagEntity tag = new TagEntity(name);
        tag = tagDao.save(tag);

        tag.setTagName(uniqueName("NewName"));
        tagDao.update(tag);

        testTags.add(tag);

        TagEntity updated = tagDao.findById(tag.getId());
        assertEquals(tag.getTagName(), updated.getTagName());
    }

    @Test
    void testUpdateDuplicateDoesNotChangeOriginal() {
        String name1 = uniqueName("TagA");
        String name2 = uniqueName("TagB");

        TagEntity tag1 = tagDao.save(new TagEntity(name1));
        TagEntity tag2 = tagDao.save(new TagEntity(name2));

        tag2.setTagName(name1);

        assertThrows(IllegalArgumentException.class, () -> tagDao.update(tag2));

        // Ensure original tag2 name unchanged in DB
        TagEntity reloaded = tagDao.findById(tag2.getId());
        assertEquals(name2, reloaded.getTagName());
    }

    @Test
    void testDeleteTag() {
        TagEntity tag = tagDao.save(new TagEntity(uniqueName("ToDelete")));

        // associate a note to ensure deletion handles removal
        NoteEntity note = new NoteEntity("Note", "Content", "Annotation");
        note.addTag(tag);

        tagDao.delete(tag);

        assertNull(tagDao.findById(tag.getId()));
    }

    @Test
    void testDeleteTagWithRealNoteAssociation() {
        TagEntity tag = tagDao.save(new TagEntity(uniqueName("TagWithNote")));

        EntityManager em = MariaDbJpaConnection.createEntityManager();
        em.getTransaction().begin();

        UserEntity user = new UserEntity();
        user.setFirstName("Test");
        user.setLastName("User");
        user.setUsername("testuser_" + System.currentTimeMillis());
        user.setEmail("test_" + System.currentTimeMillis() + "@example.com");
        user.changePasswordHash("Test@123");
        em.persist(user);

        NoteBookEntity notebook = new NoteBookEntity("Test Notebook", user);
        em.persist(notebook);

        NoteEntity note = new NoteEntity("Title", "Content", "Annotation");
        note.setNotebook(notebook);
        note.addTag(tag);

        em.persist(note);
        em.getTransaction().commit();
        em.close();

        tagDao.delete(tag);

        assertNull(tagDao.findById(tag.getId()));
    }

    @Test
    void testDeleteNonExistentTag() {
        TagEntity tag = new TagEntity("NonExistent");
        tag.setId(99999L); // fake ID
        assertDoesNotThrow(() -> tagDao.delete(tag));
    }

    @Test
    void testSaveDuplicateThrows() {
        String name = uniqueName("DuplicateTag");
        TagEntity tag1 = new TagEntity(name);
        tagDao.save(tag1);

        TagEntity tag2 = new TagEntity(name);
        assertThrows(IllegalArgumentException.class, () -> tagDao.save(tag2));
    }

    @Test
    void testUpdateDuplicateThrows() {
        String name1 = uniqueName("Tag1");
        String name2 = uniqueName("Tag2");

        TagEntity tag1 = tagDao.save(new TagEntity(name1));
        TagEntity tag2 = tagDao.save(new TagEntity(name2));

        testTags.add(tag1);
        testTags.add(tag2);

        // try to rename tag2 to name1
        tag2.setTagName(name1);
        assertThrows(IllegalArgumentException.class, () -> tagDao.update(tag2)); // or tagDao.update(tag2)
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
}