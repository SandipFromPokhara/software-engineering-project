package dao.tag;

import datasource.MariaDbJpaConnection;
import entity.NoteEntity;
import entity.TagEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class JpaTagDao implements TagDAO{

    public JpaTagDao() {}

    @Override
    public TagEntity save(TagEntity tag) {
        if (tag == null) throw new IllegalArgumentException("Tag cannot be null");
        if (tag.getId() == null && existsByName(tag.getTagName())) throw new IllegalArgumentException("Tag with this name already exists");

        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            em.getTransaction().begin();
            TagEntity managedTag;

            if (tag.getId() == null) {
                em.persist(tag);
                managedTag = tag;
            } else {
                // For updates, ensure no other tag has the same name.
                // Exclude current tag by its ID to allow renaming without conflict.
                TypedQuery<TagEntity> query = em.createQuery("Select t FROM TagEntity t WHERE t.tagName = :tagName AND t.id != :currentId", TagEntity.class);
                query.setParameter("tagName", tag.getTagName());
                query.setParameter("currentId", tag.getId());
                List<TagEntity> result = query.getResultList();
                if (!result.isEmpty()) throw new IllegalArgumentException("Tag with this name already exists");
                managedTag = em.merge(tag);
            }
            em.getTransaction().commit();

            return managedTag;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Failed to persist tag", e);
        } finally {
            em.close();
        }
    }

    @Override
    public boolean existsByName(String tagName) {
        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery("Select COUNT(t) from TagEntity t where t.tagName = :tagName", Long.class);
            query.setParameter("tagName", tagName);
            Long count = query.getSingleResult();

            return count > 0;
        } catch (Exception e) {
            throw new RuntimeException("Failed to find tag", e);
        } finally {
            em.close();
        }
    }

    @Override
    public TagEntity findByName(String tagName) {
        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            TypedQuery<TagEntity> query = em.createQuery("Select t from TagEntity t where t.tagName = :tagName", TagEntity.class);
            query.setParameter("tagName", tagName);
            List<TagEntity> result = query.getResultList();

            return result.isEmpty() ? null : result.get(0);
        } catch (Exception e) {
            throw new RuntimeException("Failed to find Tag by name: " + tagName, e);
        } finally {
            em.close();
        }
    }

    @Override
    public List<TagEntity> findAll() {
        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            TypedQuery<TagEntity> query = em.createQuery("Select t from TagEntity t", TagEntity.class);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to find tags", e);
        } finally {
            em.close();
        }
    }

    @Override
    public TagEntity findById(Long id) {
        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            return em.find(TagEntity.class, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to find Tag by ID: " + id, e);
        } finally {
            em.close();
        }
    }

    @Override
    public void update(TagEntity tag) {
        if (tag == null) throw new IllegalArgumentException("Tag cannot be null");

        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            em.getTransaction().begin();
            TypedQuery<TagEntity> query = em.createQuery("SELECT t FROM TagEntity t WHERE t.tagName = :tagName AND t.id != :currentId", TagEntity.class);
            query.setParameter("tagName", tag.getTagName());
            query.setParameter("currentId", tag.getId());
            if (!query.getResultList().isEmpty()) {
                throw new IllegalArgumentException("Tag with this name already exists");
            }

            em.merge(tag);
            em.getTransaction().commit();
        } catch (IllegalArgumentException  e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(TagEntity tag) {
        if (tag == null) throw new IllegalArgumentException("Tag cannot be null");

        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            em.getTransaction().begin();
            TagEntity managedTag = em.find(TagEntity.class, tag.getId());

            if (managedTag != null) {
                for (NoteEntity note : managedTag.getNotes()) {
                    note.removeTag(managedTag);
                }
                em.remove(managedTag);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Failed to delete tag: " + tag, e);
        } finally {
            em.close();
        }
    }
}
