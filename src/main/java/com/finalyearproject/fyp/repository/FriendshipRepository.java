package com.finalyearproject.fyp.repository;

import com.finalyearproject.fyp.entity.Friendship;
import com.finalyearproject.fyp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    // Find friendship between two users regardless of direction
    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.requester = :user1 AND f.addressee = :user2) OR " +
            "(f.requester = :user2 AND f.addressee = :user1)")
    Optional<Friendship> findBetween(@Param("user1") User user1, @Param("user2") User user2);

    // All accepted friends for a user
    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.requester = :user OR f.addressee = :user) AND f.status = 'ACCEPTED'")
    List<Friendship> findAcceptedFriendships(@Param("user") User user);

    // Incoming pending requests (others sent to me)
    @Query("SELECT f FROM Friendship f WHERE f.addressee = :user AND f.status = 'PENDING'")
    List<Friendship> findIncomingRequests(@Param("user") User user);

    // Outgoing pending requests (I sent to others)
    @Query("SELECT f FROM Friendship f WHERE f.requester = :user AND f.status = 'PENDING'")
    List<Friendship> findOutgoingRequests(@Param("user") User user);
}