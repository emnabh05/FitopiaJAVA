package tn.esprit.Pidev3A49.interfaces;

import tn.esprit.Pidev3A49.Models.Personne;

import java.util.List;

public interface IServices<T> {

    void add (T t);
    List<T> getAll();
    void update (T t) ;
    void delete(T t);
}
