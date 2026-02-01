package dao.baseDAO;

public interface GenericDAO<T> {

    void save(T entity);

    T findById(Long id);

    void delete(T entity);
}
