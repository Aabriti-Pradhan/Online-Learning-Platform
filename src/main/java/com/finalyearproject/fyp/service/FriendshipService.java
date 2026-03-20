package com.finalyearproject.fyp.service;

import com.finalyearproject.fyp.dto.FriendsPageDTO;

public interface FriendshipService {

    /** Loads all data needed for the friends page for the given user. */
    FriendsPageDTO getFriendsPageData(String email);

    /** Send a friend request from the logged-in user to the target. */
    void sendRequest(String requesterEmail, Long targetUserId);

    /** Accept an incoming friend request. Throws if not the addressee. */
    void acceptRequest(Long friendshipId, String addresseeEmail);

    /** Decline and delete an incoming friend request. */
    void declineRequest(Long friendshipId, String addresseeEmail);

    /** Remove an accepted friendship. */
    void removeFriend(Long friendshipId, String requesterEmail);

    /** Cancel an outgoing pending request. */
    void cancelRequest(String requesterEmail, Long targetUserId);
}