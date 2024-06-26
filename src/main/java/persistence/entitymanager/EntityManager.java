package persistence.entitymanager;

public interface EntityManager {

    <T> T find(Class<T> clazz, Long Id);

    void persist(Object entity);

    void remove(Object entity);

    void flush();

    void clear();
}
