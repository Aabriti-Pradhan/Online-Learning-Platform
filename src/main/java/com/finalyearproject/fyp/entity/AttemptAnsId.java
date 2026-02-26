package com.finalyearproject.fyp.entity;

import java.io.Serializable;
import java.util.Objects;

public class AttemptAnsId implements Serializable {

    private Long attemptId;
    private Long questionId;

    public AttemptAnsId() {}

    public AttemptAnsId(Long attemptId, Long questionId) {
        this.attemptId = attemptId;
        this.questionId = questionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AttemptAnsId)) return false;
        AttemptAnsId that = (AttemptAnsId) o;
        return Objects.equals(attemptId, that.attemptId) &&
                Objects.equals(questionId, that.questionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attemptId, questionId);
    }
}