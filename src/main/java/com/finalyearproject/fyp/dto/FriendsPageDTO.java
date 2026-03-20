package com.finalyearproject.fyp.dto;

import java.util.List;
import java.util.Set;

public record FriendsPageDTO(
        List<FriendDTO>        friends,
        List<FriendRequestDTO> incoming,
        Set<Long>              pendingOutgoing,
        Set<Long>              friendIds,
        List<UserDTO>          allUsers
) {}