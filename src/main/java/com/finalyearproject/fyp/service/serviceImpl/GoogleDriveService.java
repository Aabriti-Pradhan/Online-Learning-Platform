package com.finalyearproject.fyp.service.serviceImpl;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@Service
public class GoogleDriveService {

    @Autowired
    private OAuth2AuthorizedClientService clientService;

    public void uploadToDrive(MultipartFile multipartFile) throws Exception {

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
                request -> request.getHeaders().setAuthorization("Bearer " + accessToken)
        ).setApplicationName("Prep Hunt").build();

        String prepHuntFolderId =
                createFolderIfNotExists(drive, "Prep Hunt", null);

        String resourceFolderId =
                createFolderIfNotExists(drive, "Your Resources", prepHuntFolderId);

        File fileMeta = new File();
        fileMeta.setName(multipartFile.getOriginalFilename());
        fileMeta.setParents(Collections.singletonList(resourceFolderId));

        InputStreamContent content = new InputStreamContent(
                multipartFile.getContentType(),
                multipartFile.getInputStream()
        );

        drive.files()
                .create(fileMeta, content)
                .setFields("id")
                .execute();
    }

    private String createFolderIfNotExists(Drive drive,
                                           String name,
                                           String parentId) throws Exception {

        String query = "mimeType='application/vnd.google-apps.folder' and name='"
                + name + "' and trashed=false";

        if (parentId != null) {
            query += " and '" + parentId + "' in parents";
        }

        var result = drive.files()
                .list()
                .setQ(query)
                .setFields("files(id, name)")
                .execute();

        if (result.getFiles() != null && !result.getFiles().isEmpty()) {
            return result.getFiles().get(0).getId();
        }

        File fileMeta = new File();
        fileMeta.setName(name);
        fileMeta.setMimeType("application/vnd.google-apps.folder");

        if (parentId != null) {
            fileMeta.setParents(Collections.singletonList(parentId));
        }

        File folder = drive.files()
                .create(fileMeta)
                .setFields("id")
                .execute();

        return folder.getId();
    }

    public List<com.google.api.services.drive.model.File> listPDFs(OAuth2AuthenticationToken oauthToken) throws Exception {

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
                com.google.api.client.json.gson.GsonFactory.getDefaultInstance(),
                request -> request.getHeaders().setAuthorization("Bearer " + accessToken)
        ).setApplicationName("Prep Hunt").build();

        String prepHuntId = findFolderId(drive, "Prep Hunt", null);
        String resourceFolderId = findFolderId(drive, "Your Resources", prepHuntId);

        var result = drive.files().list()
                .setQ("'" + resourceFolderId + "' in parents and mimeType='application/pdf'")
                .setFields("files(id,name,webViewLink,createdTime)")
                .execute();

        return result.getFiles();
    }

    private String findFolderId(Drive drive, String name, String parentId) throws Exception {

        String query = "mimeType='application/vnd.google-apps.folder' and name='" + name + "'";

        if (parentId != null) {
            query += " and '" + parentId + "' in parents";
        }

        FileList result = drive.files().list()
                .setQ(query)
                .setFields("files(id,name)")
                .execute();

        if (!result.getFiles().isEmpty()) {
            return result.getFiles().get(0).getId();
        }

        return null;
    }
}