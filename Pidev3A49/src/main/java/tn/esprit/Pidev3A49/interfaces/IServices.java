package tn.esprit.Pidev3A49.interfaces;

import java.util.List;

public interface IServices<T> {

    void add(T t);

    List<T> getAll();
}
