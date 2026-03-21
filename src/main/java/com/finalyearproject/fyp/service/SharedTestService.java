package com.finalyearproject.fyp.service;

import com.finalyearproject.fyp.dto.ShareTestRequest;
import com.finalyearproject.fyp.dto.SharedTestDTO;

import java.util.List;

public interface SharedTestService {

    /** Share a test with one or more friends. */
    void shareTest(String senderEmail, ShareTestRequest request);

    /** Get all tests shared WITH the logged-in user. */
    List<SharedTestDTO> getTestsSharedWithMe(String userEmail);

    /** Get all tests the logged-in user has shared to others. */
    List<SharedTestDTO> getTestsSharedByMe(String userEmail);

    /** Get list of friend IDs this test has already been shared with by the sender. */
    List<Long> getAlreadySharedFriendIds(Long testId, String senderEmail);
}