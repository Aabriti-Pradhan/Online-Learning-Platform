package com.finalyearproject.fyp.service.serviceImpl;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import com.google.api.services.drive.model.File;
import java.util.Collections;
import java.util.List;

@Service
public class CourseDriveService {

    @Autowired
    private OAuth2AuthorizedClientService clientService;

    public String createCourseFolder(String courseName) throws Exception {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        OAuth2AuthenticationToken oauthToken =
                (OAuth2AuthenticationToken) authentication;

        OAuth2AuthorizedClient client =
                clientService.loadAuthorizedClient(
                        oauthToken.getAuthorizedClientRegistrationId(),
                        oauthToken.getName()
                );

        String accessToken = client.getAccessToken().getTokenValue();

        Drive drive = new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                request -> request.getHeaders().setAuthorization("Bearer " + accessToken)
        ).setApplicationName("Prep Hunt").build();

        // Get or Create Prep Hunt folder
        String prepHuntId = createFolderIfNotExists(drive, "Prep Hunt", null);

        File folderMeta = new File();
        folderMeta.setName(courseName);
        folderMeta.setMimeType("application/vnd.google-apps.folder");
        folderMeta.setParents(Collections.singletonList(prepHuntId));

        File createdFolder = drive.files()
                .create(folderMeta)
                .setFields("id, webViewLink")
                .execute();

        return createdFolder.getId();
    }

    private String createFolderIfNotExists(Drive drive, String name, String parentId) throws Exception {

        // Query for folder with this name
        String query = "mimeType='application/vnd.google-apps.folder' and name='" + name + "' and trashed=false";
        if (parentId != null) {
            query += " and '" + parentId + "' in parents";
        }

        var result = drive.files()
                .list()
                .setQ(query)
                .setFields("files(id,name)")
                .execute();

        if (!result.getFiles().isEmpty()) {
            // Folder exists → return its ID
            return result.getFiles().get(0).getId();
        }

        // Folder does not exist -> create it
        File folderMeta = new File();
        folderMeta.setName(name);
        folderMeta.setMimeType("application/vnd.google-apps.folder");
        if (parentId != null) {
            folderMeta.setParents(Collections.singletonList(parentId));
        }

        File folder = drive.files()
                .create(folderMeta)
                .setFields("id")
                .execute();

        return folder.getId();
    }

    public List<File> listFilesInsideFolder(String folderId,
                                            OAuth2AuthenticationToken oauthToken) throws Exception {

        OAuth2AuthorizedClient client =
                clientService.loadAuthorizedClient(
                        oauthToken.getAuthorizedClientRegistrationId(),
                        oauthToken.getName()
                );

        String accessToken = client.getAccessToken().getTokenValue();

        Drive drive = new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                request -> request.getHeaders().setAuthorization("Bearer " + accessToken)
        ).setApplicationName("Prep Hunt").build();

        var result = drive.files().list()
                .setQ("'" + folderId + "' in parents and trashed=false")
                .setFields("files(id,name,mimeType,webViewLink)")
                .execute();

        return result.getFiles();
    }

    public void deleteCourseFolder(String folderId) throws Exception {

        if (folderId == null || folderId.isEmpty()) {
            return;
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
            throw new IllegalStateException("User is not authenticated with OAuth2");
        }

        OAuth2AuthorizedClient client =
                clientService.loadAuthorizedClient(
                        oauthToken.getAuthorizedClientRegistrationId(),
                        oauthToken.getName()
                );

        if (client == null || client.getAccessToken() == null) {
            throw new IllegalStateException("Access token not found");
        }

        String accessToken = client.getAccessToken().getTokenValue();

        Drive drive = new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                request -> request.getHeaders()
                        .setAuthorization("Bearer " + accessToken)
        ).setApplicationName("Prep Hunt").build();

        drive.files().delete(folderId).execute();
    }
}