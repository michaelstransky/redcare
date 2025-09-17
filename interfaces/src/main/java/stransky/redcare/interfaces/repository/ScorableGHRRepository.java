package stransky.redcare.interfaces.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Repository for a Scorable GitHub Repository. The name of the Entity abbreviated to avoid confusion with this Repository.
 */
@Repository
public interface ScorableGHRRepository extends JpaRepository<ScorableGHR, String> {

    @Transactional(readOnly = true)
    @Query("FROM ScorableGHR WHERE language = ?1 AND createdAt >= ?2")
    Object[] find(String language, Instant createdAt);

    @Transactional(readOnly = true)
    @Query("SELECT MAX(updatedAt) FROM ScorableGHR WHERE language = ?1")
    Instant newest(String language);

    @Transactional(readOnly = true)
    @Query("SELECT MIN(updatedAt) FROM ScorableGHR WHERE language = ?1")
    Instant oldest(String language);
}
