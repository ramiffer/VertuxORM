package org.project.entityManager.entity;


public interface EntityManager extends AutoCloseable {

    /**
     * CRUD BASICO
     */
    <T> T find(Class<T> entityClass, Object id);
    <T> void persist(T entity);
    <T> T merge(T entity);
    void remove(Object entity);

    /**
     * Manejo de queries
     */
    //<T> Query<T> createQuery(String jpql, Class<T> resultClass);
    //<T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass);


    /**
     * Gestion de estado
     */
    void flush();
    void clear();
    void detach(Object entity);


}
