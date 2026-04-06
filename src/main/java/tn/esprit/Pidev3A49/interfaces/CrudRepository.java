package tn.esprit.Pidev3A49.interfaces;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface CrudRepository<T, ID> {
    void add(T entity) throws SQLException;

    void update(T entity) throws SQLException;

    void deleteById(ID id) throws SQLException;

    Optional<T> findById(ID id) throws SQLException;

    List<T> findAll() throws SQLException;
}
