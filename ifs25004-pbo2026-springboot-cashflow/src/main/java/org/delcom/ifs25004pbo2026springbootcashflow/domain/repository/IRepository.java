package org.delcom.ifs25004pbo2026springbootcashflow.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Port penyimpanan generik.
 *
 * @param <T>  tipe entitas
 * @param <ID> tipe identifier entitas
 */
public interface IRepository<T, ID> {

    /** Mengambil semua entitas (salinan defensif). */
    List<T> findAll();

    Optional<T> findById(ID id);

    T save(T entity);

    /** Menghapus berdasarkan ID; true jika ada yang terhapus. */
    boolean deleteById(ID id);

    void update(T entity);

    List<T> findBy(Predicate<T> predicate);
}
