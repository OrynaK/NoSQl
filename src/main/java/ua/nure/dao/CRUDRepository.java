package ua.nure.dao;

import java.util.List;

public interface CRUDRepository<T> {
    String add(T entity);

    T update(T entity);

    void delete(String id);

    T findById(String id);

    List<T> findAll();
}
