package com.finalyearproject.fyp.repository;

import com.finalyearproject.fyp.entity.User;
import com.finalyearproject.fyp.entity.UserPredict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPredictRepository extends JpaRepository<UserPredict, Long> {

    // All predictions for a user
    @Query("SELECT up FROM UserPredict up WHERE up.user = :user ORDER BY up.prediction.generatedAt DESC")
    List<UserPredict> findByUser(@Param("user") User user);

    // Latest prediction of a given type for a user
    @Query("SELECT up FROM UserPredict up WHERE up.user = :user AND up.prediction.predictionType = :type ORDER BY up.prediction.generatedAt DESC")
    List<UserPredict> findByUserAndType(@Param("user") User user, @Param("type") String type);
}