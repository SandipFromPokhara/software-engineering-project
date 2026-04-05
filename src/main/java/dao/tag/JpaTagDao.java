package dao.tag;

import dao.basedao.GenericAbstractDAO;
import entity.NoteEntity;
import entity.TagEntity;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class JpaTagDao extends GenericAbstractDAO<TagEntity, Long> implements TagDAO{

    public JpaTagDao() {}

    @Override
    public TagEntity save(TagEntity tag) {
        if (tag == null) throw new IllegalArgumentException("Tag cannot be null");
        if (tag.getId() == null && existsByName(tag.getTagName())) throw new IllegalArgumentException("Tag with this name already exists");

        return executeInTransaction(em -> {
            if (tag.getId() == null) {
                em.persist(tag);
                return tag;
            } else {
                // For updates, ensure no other tag has the same name.
                // Exclude current tag by its ID to allow renaming without conflict.
                TypedQuery<TagEntity> query = em.createQuery("SELECT t FROM TagEntity t WHERE t.tagName = :tagName AND t.id != :currentId", TagEntity.class);
                query.setParameter("tagName", tag.getTagName());
                query.setParameter("currentId", tag.getId());
                List<TagEntity> result = query.getResultList();
                if (!result.isEmpty()) throw new IllegalArgumentException("Tag with this name already exists");
                return em.merge(tag);
            }
        });
    }

    @Override
    public boolean existsByName(String tagName) {
        return executeInTransaction(em -> {
            TypedQuery<Long> query = em.createQuery("SELECT COUNT(t) FROM TagEntity t WHERE t.tagName = :tagName", Long.class);
            query.setParameter("tagName", tagName);
            Long count = query.getSingleResult();

            return count > 0;
        });
    }

    @Override
    public TagEntity findByName(String tagName) {
        return executeInTransaction(em -> {
            TypedQuery<TagEntity> query = em.createQuery("SELECT t FROM TagEntity t WHERE t.tagName = :tagName", TagEntity.class);
            query.setParameter("tagName", tagName);
            List<TagEntity> result = query.getResultList();

            return result.isEmpty() ? null : result.get(0);
        });
    }

    @Override
    public List<TagEntity> findAll() {
        return executeInTransaction(em -> {
            TypedQuery<TagEntity> query = em.createQuery("SELECT t FROM TagEntity t", TagEntity.class);
            return query.getResultList();
        });
    }

    @Override
    public TagEntity findById(Long id) {
        return executeInTransaction(em -> em.find(TagEntity.class, id));
    }

    @Override
    public void update(TagEntity tag) {
        if (tag == null) throw new IllegalArgumentException("Tag cannot be null");

        executeInTransaction(em -> {
            TypedQuery<TagEntity> query = em.createQuery("SELECT t FROM TagEntity t WHERE t.tagName = :tagName AND t.id != :currentId", TagEntity.class);
            query.setParameter("tagName", tag.getTagName());
            query.setParameter("currentId", tag.getId());
            if (!query.getResultList().isEmpty()) {
                throw new IllegalArgumentException("Tag with this name already exists");
            }

            em.merge(tag);

            return null;
        });
    }

    @Override
    public void delete(TagEntity tag) {
        if (tag == null) throw new IllegalArgumentException("Tag cannot be null");

        executeInTransaction(em -> {
            TagEntity managedTag = em.find(TagEntity.class, tag.getId());

            if (managedTag != null) {
                for (NoteEntity note : managedTag.getNotes()) {
                    note.removeTag(managedTag);
                }
                em.remove(managedTag);
            }

            return null;
        });
    }
}