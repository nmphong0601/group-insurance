package jp.xet.sparwings.spring.data.repository;

import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

/**
 * Generic Database Repository interface that combines scanning, batch reading, and batch writing capabilities.
 * Extends from ScannableRepository, BatchReadableRepository, and BatchWritableRepository.
 *
 * @param <E>  the entity type
 * @param <ID> the entity identifier type, must be Serializable
 */
@NoRepositoryBean
public interface DbRepository<E, ID extends Serializable>
        extends ScannableRepository<E, ID>,
                BatchReadableRepository<E, ID>,
                BatchWritableRepository<E, ID> {
}
