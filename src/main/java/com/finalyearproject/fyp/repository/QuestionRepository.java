package com.finalyearproject.fyp.repository;

import com.finalyearproject.fyp.entity.Question;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM Question q WHERE q.questionId IN :questionIds")
    void deleteByQuestionIds(@Param("questionIds") Set<Long> questionIds);
}