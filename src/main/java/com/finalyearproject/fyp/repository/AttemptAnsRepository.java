package com.finalyearproject.fyp.repository;

import com.finalyearproject.fyp.entity.AttemptAns;
import com.finalyearproject.fyp.entity.AttemptAnsId;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface AttemptAnsRepository extends JpaRepository<AttemptAns, AttemptAnsId> {
    @Query("SELECT a FROM AttemptAns a WHERE a.attemptId = :attemptId")
    List<AttemptAns> findByAttemptId(@Param("attemptId") Long attemptId);

    @Modifying
    @Transactional
    @Query("DELETE FROM AttemptAns aa WHERE aa.attemptId IN :attemptIds")
    void deleteByAttemptIds(@Param("attemptIds") Set<Long> attemptIds);
}