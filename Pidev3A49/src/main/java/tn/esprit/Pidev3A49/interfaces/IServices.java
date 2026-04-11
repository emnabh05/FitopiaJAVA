package tn.esprit.Pidev3A49.interfaces;

import java.util.List;

public interface IServices<T> {

    void add(T t);

    List<T> getAll();

    default T getById(int id) {
        return null;
    }

    void update(T t);

    void delete(T t);

    default void deleteById(int id) {
        T entity = getById(id);
        if (entity == null) {
            throw new IllegalArgumentException("Aucune entite trouvee avec l'id " + id);
        }
        delete(entity);
    }
}
