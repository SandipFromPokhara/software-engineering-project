package dao.tag;

import dao.basedao.GenericAbstractDAO;
import entity.entities.NoteEntity;
import entity.entities.TagEntity;
import jakarta.persistence.TypedQuery;

import java.util.HashSet;
import java.util.List;

public class JpaTagDao extends GenericAbstractDAO<TagEntity, Long> implements ITagDAO {

    public JpaTagDao() {}

    @Override
    public TagEntity save(TagEntity tag) {
        if (tag == null) throw new IllegalArgumentException("Tag cannot be null");

        return executeInTransaction(em -> {
            try {
                if (tag.getId() == null) {
                    em.persist(tag);
                    return tag;
                } else {
                    return em.merge(tag);
                }
            } catch (Exception e) {
                if (isUniqueConstraintViolation(e)) {
                    throw new IllegalArgumentException("Tag already exists");
                }
                throw e;
            }
        });
    }

    @Override
    public boolean existsByName(String tagName) {
        return execute(em -> {
            TypedQuery<Long> query = em.createQuery("SELECT COUNT(t) FROM TagEntity t WHERE LOWER(t.tagName) = LOWER(:tagName)", Long.class);
            query.setParameter("tagName", tagName);

            Long count = query.getSingleResult();
            return count > 0;
        });
    }

    @Override
    public TagEntity findByName(String tagName) {
        return execute(em -> {
            TypedQuery<TagEntity> query = em.createQuery("SELECT t FROM TagEntity t WHERE LOWER(t.tagName) = LOWER(:tagName)", TagEntity.class);

            query.setParameter("tagName", tagName);

            List<TagEntity> result = query.getResultList();
            return result.isEmpty() ? null : result.get(0);
        });
    }

    @Override
    public List<TagEntity> findAll() {
        return execute(em -> {
            TypedQuery<TagEntity> query = em.createQuery("SELECT DISTINCT t FROM TagEntity t", TagEntity.class);
            return query.getResultList();
        });
    }

    @Override
    public TagEntity findById(Long id) {
        return execute(em -> em.createQuery(
                        "SELECT t FROM TagEntity t LEFT JOIN FETCH t.notes WHERE t.id = :id", TagEntity.class)
                .setParameter("id", id)
                .getSingleResult());
    }

    @Override
    public void update(TagEntity tag) {
        if (tag == null) throw new IllegalArgumentException("Tag cannot be null");

        executeInTransaction(em -> {
            TypedQuery<TagEntity> query = em.createQuery(
                    "SELECT t FROM TagEntity t WHERE LOWER(t.tagName) = LOWER(:tagName)" +
                            "AND t.id != :currentId",
                    TagEntity.class
            );

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
            TagEntity managedTag = em.createQuery("SELECT t FROM TagEntity t LEFT JOIN FETCH t.notes WHERE t.id = :id", TagEntity.class)
                    .setParameter("id", tag.getId())
                    .getSingleResult();

            if (managedTag != null) {
                for (NoteEntity note : new HashSet<>(managedTag.getNotes())) {
                    note.getTags().remove(managedTag);
                }
                em.remove(managedTag);
            }

            return null;
        });
    }
}