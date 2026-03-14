package com.finalyearproject.fyp.repository;

import com.finalyearproject.fyp.entity.AttemptAns;
import com.finalyearproject.fyp.entity.AttemptAnsId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttemptAnsRepository extends JpaRepository<AttemptAns, AttemptAnsId> {
    @Query("SELECT a FROM AttemptAns a WHERE a.attemptId = :attemptId")
    List<AttemptAns> findByAttemptId(@Param("attemptId") Long attemptId);
}