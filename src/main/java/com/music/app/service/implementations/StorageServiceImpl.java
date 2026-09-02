package com.music.app.service.implementations;

import com.music.app.exception.StorageNotFoundException;
import com.music.app.service.interfaces.StorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class StorageServiceImpl implements StorageService {

    private final Path root = Paths.get("uploads");

    public StorageServiceImpl() {
        init();
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directory", e);
        }
    }

    @Override
    public void store(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store an empty file.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "file_" + System.currentTimeMillis();
        }

        // Sanitize path to prevent directory traversal
        String cleanFileName = Paths.get(originalFilename).getFileName().toString();
        Path destination = this.root.resolve(cleanFileName).normalize().toAbsolutePath();

        // Safely open and close input stream with try-with-resources
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Override
    public Stream<Path> loadAll() throws IOException {
        return Files.walk(this.root, 1)
                .filter(path -> !path.equals(this.root))
                .filter(Files::isRegularFile);
    }

    @Override
    public Path load(String fileName) {
        return root.resolve(fileName).normalize();
    }

    @Override
    public Resource loadAsResource(String filename) throws MalformedURLException {
        Path file = load(filename);
        Resource resource = new UrlResource(file.toUri());

        if (resource.exists() && resource.isReadable()) {
            return resource;
        }

        throw new StorageNotFoundException("File not found or not readable: " + filename);
    }

    @Override
    public void delete(String filename) throws IOException {
        Path file = load(filename);
        Files.deleteIfExists(file);
    }
}