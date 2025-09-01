package com.repo_gestion_tr.repository;

import java.util.List;

public interface Repository<T> {
    public T save(T entity);
    public T findById(int id);
    public List<T> findAll();
}
