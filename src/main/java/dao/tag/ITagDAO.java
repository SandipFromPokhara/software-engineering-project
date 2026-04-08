package dao.tag;

import dao.basedao.IGenericDAO;
import entity.entities.TagEntity;

import java.util.List;

public interface ITagDAO extends IGenericDAO<TagEntity, Long> {
    TagEntity findByName(String name);
    List<TagEntity> findAll();
    boolean existsByName(String name);
}