package com.music.app.service.interfaces;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {

    void init();

    void store(MultipartFile file) throws IOException;

    Stream<Path> loadAll() throws IOException;

    Path load(String filename) throws IOException;

    Resource loadAsResource(String filename) throws MalformedURLException;

    void delete(String filename) throws IOException;
}
