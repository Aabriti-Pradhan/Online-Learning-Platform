package com.finalyearproject.fyp.controller;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Wraps a raw byte array as a MultipartFile so it can be passed to
 * LocalFileStorageService.store() without needing an actual HTTP upload.
 */
public class ByteArrayMultipartFile implements MultipartFile {

    private final String name;
    private final byte[] content;

    public ByteArrayMultipartFile(String name, byte[] content) {
        this.name    = name;
        this.content = content;
    }

    @Override public String getName()                  { return name; }
    @Override public String getOriginalFilename()      { return name; }
    @Override public String getContentType()           { return "application/json"; }
    @Override public boolean isEmpty()                 { return content.length == 0; }
    @Override public long getSize()                    { return content.length; }
    @Override public byte[] getBytes()                 { return content; }
    @Override public InputStream getInputStream()      { return new ByteArrayInputStream(content); }
    @Override public void transferTo(File dest) throws IOException {
        java.nio.file.Files.write(dest.toPath(), content);
    }
}