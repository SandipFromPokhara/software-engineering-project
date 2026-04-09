package dao.basedao;

public interface IGenericDAO<T, ID> {

    T save(T entity);

    T findById(ID id);

    void delete(T entity);

    void update(T entity);
}
