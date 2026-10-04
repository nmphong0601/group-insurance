package vn.com.group.insurance.common.support;

import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import vn.com.group.insurance.common.repository.DbRepository;

import java.io.Serializable;

// 1. Lives ONLY in Common. Proposal module never touches this.
public class DbRepositoryImpl<E, ID extends Serializable>
        extends SimpleJpaRepository<E, ID>
        implements DbRepository<E, ID> {

    private final JpaEntityInformation<E, ?> entityInformation;
    private final EntityManager entityManager;

    public DbRepositoryImpl(JpaEntityInformation<E, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityInformation = entityInformation;
        this.entityManager = entityManager;
    }

    // --- Implementing ReadableRepository ---
    @Override
    public boolean exists(ID id) {
        return super.existsById(id); // Maps to Spring Data
    }

    @Override
    public E findOne(ID id) {
        return super.findById(id).orElse(null); // Maps to Spring Data
    }

    @Override
    @SuppressWarnings("unchecked")
    public ID getId(E entity) {
        return (ID) entityInformation.getId(entity); // Fixes the QueryCreationException!
    }

    @Override
    public Iterable<E> findAll(Iterable<ID> ids) {
        return null;
    }

    @Override
    public void delete(Iterable<? extends E> entities) {

    }

    @Override
    public <S extends E> Iterable<S> save(Iterable<S> entities) {
        return null;
    }

    @Override
    public <S extends E> S create(S entity) {
        return null;
    }

    @Override
    public void delete(ID id) {

    }

    @Override
    public <S extends E> S update(S entity) {
        return null;
    }

    // (Implement your Batch/Writable methods here too)
}
