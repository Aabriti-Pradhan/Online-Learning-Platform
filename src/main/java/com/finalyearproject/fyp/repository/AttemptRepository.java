package com.finalyearproject.fyp.repository;

import com.finalyearproject.fyp.entity.Attempt;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface AttemptRepository extends JpaRepository<Attempt, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM Attempt a WHERE a.attemptId IN :attemptIds")
    void deleteByAttemptIds(@Param("attemptIds") Set<Long> attemptIds);
}