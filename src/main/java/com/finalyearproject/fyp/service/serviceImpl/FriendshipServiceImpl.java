package com.finalyearproject.fyp.service.serviceImpl;

import com.finalyearproject.fyp.dto.*;
import com.finalyearproject.fyp.entity.Friendship;
import com.finalyearproject.fyp.entity.User;
import com.finalyearproject.fyp.repository.FriendshipRepository;
import com.finalyearproject.fyp.repository.UserRepository;
import com.finalyearproject.fyp.service.FriendshipService;
import com.finalyearproject.fyp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendshipServiceImpl implements FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository       userRepository;
    private final NotificationService  notificationService;

    // Friends page data

    @Override
    public FriendsPageDTO getFriendsPageData(String email) {
        User me = getByEmail(email);

        List<FriendDTO> friends = friendshipRepository.findAcceptedFriendships(me)
                .stream().map(f -> {
                    User other = f.getRequester().getUserId().equals(me.getUserId())
                            ? f.getAddressee() : f.getRequester();
                    return new FriendDTO(
                            other.getUserId(),
                            other.getUsername(),
                            other.getEmail(),
                            other.getRole(),
                            f.getFriendshipId()
                    );
                }).toList();

        List<FriendRequestDTO> incoming = friendshipRepository.findIncomingRequests(me)
                .stream().map(f -> new FriendRequestDTO(
                        f.getFriendshipId(),
                        f.getRequester().getUserId(),
                        f.getRequester().getUsername(),
                        f.getRequester().getEmail(),
                        f.getRequester().getRole(),
                        f.getCreatedAt()
                )).toList();

        Set<Long> pendingOutgoing = friendshipRepository.findOutgoingRequests(me)
                .stream().map(f -> f.getAddressee().getUserId())
                .collect(Collectors.toSet());

        Set<Long> friendIds = friends.stream()
                .map(FriendDTO::userId)
                .collect(Collectors.toSet());

        List<UserDTO> allUsers = userRepository.findAll().stream()
                .filter(u -> !u.getUserId().equals(me.getUserId()))
                .map(u -> new UserDTO(u.getUserId(), u.getUsername(), u.getEmail(), u.getRole()))
                .toList();

        return new FriendsPageDTO(friends, incoming, pendingOutgoing, friendIds, allUsers);
    }

    // Send request

    @Override
    @Transactional
    public void sendRequest(String requesterEmail, Long targetUserId) {
        User me     = getByEmail(requesterEmail);
        User target = getUserById(targetUserId);

        if (friendshipRepository.findBetween(me, target).isPresent()) {
            throw new IllegalStateException("Already connected with this user");
        }

        Friendship f = new Friendship();
        f.setRequester(me);
        f.setAddressee(target);
        f.setStatus("PENDING");
        f.setCreatedAt(LocalDateTime.now());
        friendshipRepository.save(f);

        // Notify addressee
        notificationService.sendToUser(
                target.getUserId(),
                "New Friend Request",
                me.getUsername() + " sent you a friend request.",
                "FRIEND_REQUEST",
                me.getUserId()
        );
    }

    // Accept request

    @Override
    @Transactional
    public void acceptRequest(Long friendshipId, String addresseeEmail) {
        Friendship f  = getFriendshipById(friendshipId);
        User       me = getByEmail(addresseeEmail);

        if (!f.getAddressee().getUserId().equals(me.getUserId())) {
            throw new SecurityException("Only the addressee can accept this request");
        }

        f.setStatus("ACCEPTED");
        f.setUpdatedAt(LocalDateTime.now());
        friendshipRepository.save(f);

        // Notify the original requester
        notificationService.sendToUser(
                f.getRequester().getUserId(),
                "Friend Request Accepted",
                me.getUsername() + " accepted your friend request.",
                "FRIEND_ACCEPTED",
                me.getUserId()
        );
    }

    // Decline request

    @Override
    @Transactional
    public void declineRequest(Long friendshipId, String addresseeEmail) {
        Friendship f  = getFriendshipById(friendshipId);
        User       me = getByEmail(addresseeEmail);
        if (!f.getAddressee().getUserId().equals(me.getUserId())) {
            throw new SecurityException("Only the addressee can decline this request");
        }

        friendshipRepository.delete(f);
    }

    // Remove friend

    @Override
    @Transactional
    public void removeFriend(Long friendshipId, String userEmail) {
        Friendship f  = getFriendshipById(friendshipId);
        User       me = getByEmail(userEmail);

        boolean involved = f.getRequester().getUserId().equals(me.getUserId())
                || f.getAddressee().getUserId().equals(me.getUserId());

        if (!involved) {
            throw new SecurityException("You are not part of this friendship");
        }

        friendshipRepository.delete(f);
    }

    // Cancel outgoing request

    @Override
    @Transactional
    public void cancelRequest(String requesterEmail, Long targetUserId) {
        User me     = getByEmail(requesterEmail);
        User target = getUserById(targetUserId);
        friendshipRepository.findBetween(me, target)
                .ifPresent(friendshipRepository::delete);
    }

    // Private helpers

    private User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }

    private Friendship getFriendshipById(Long friendshipId) {
        return friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Friendship not found: " + friendshipId));
    }
}