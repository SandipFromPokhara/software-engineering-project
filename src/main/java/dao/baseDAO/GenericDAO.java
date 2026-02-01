package dao.baseDAO;

public interface GenericDAO<T, ID> {

    void save(T entity);

    T findById(ID id);

    void delete(T entity);
}
