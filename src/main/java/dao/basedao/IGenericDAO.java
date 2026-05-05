package dao.basedao;

public interface IGenericDAO<T, I> {

    T save(T entity);

    T findById(I id);

    void delete(T entity);

    void update(T entity);
}
