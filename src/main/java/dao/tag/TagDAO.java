package dao.tag;

import dao.basedao.GenericDAO;
import entity.TagEntity;

import java.util.List;

public interface TagDAO extends GenericDAO<TagEntity, Long> {
    TagEntity findByName(String name);
    List<TagEntity> findAll();
    boolean existsByName(String name);
}
