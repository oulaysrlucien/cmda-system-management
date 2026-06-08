package org.cmda.management.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class MemberPhotoStorageService {

    private static final long MAX_SIZE_BYTES = 5L * 1024L * 1024L;
    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp"
    );
    private static final Set<String> ALLOWED_CONTENT_TYPES = EXTENSIONS.keySet();

    private final Path storageDirectory;

    public MemberPhotoStorageService(
            @Value("${cmda.member-photo.storage-directory:./uploads/member-photos}") String storageDirectory
    ) {
        this.storageDirectory = Path.of(storageDirectory).toAbsolutePath().normalize();
    }

    public String store(MultipartFile file) {
        validate(file);
        String contentType = file.getContentType();
        String filename = UUID.randomUUID() + EXTENSIONS.get(contentType);

        try {
            Files.createDirectories(storageDirectory);
            Files.copy(file.getInputStream(), resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to store the member photo.");
        }
    }

    public Resource load(String filename) {
        try {
            Path file = resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Member photo not found.");
            }
            return resource;
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Member photo not found.");
        }
    }

    public String contentType(String filename) {
        try {
            String contentType = Files.probeContentType(resolve(filename));
            return contentType != null ? contentType : "application/octet-stream";
        } catch (IOException exception) {
            return "application/octet-stream";
        }
    }

    public void deleteIfPresent(String filename) {
        if (filename == null || filename.isBlank()) return;
        try {
            Files.deleteIfExists(resolve(filename));
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to delete the member photo.");
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A photo file is required.");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The member photo must not exceed 5 MB.");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only JPEG, PNG and WebP photos are accepted.");
        }
        if (!hasExpectedSignature(file, file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The uploaded file is not a valid image.");
        }
    }

    private boolean hasExpectedSignature(MultipartFile file, String contentType) {
        try (InputStream input = file.getInputStream()) {
            byte[] header = input.readNBytes(12);
            return switch (contentType) {
                case "image/jpeg" -> header.length >= 3
                        && header[0] == (byte) 0xff && header[1] == (byte) 0xd8 && header[2] == (byte) 0xff;
                case "image/png" -> header.length >= 8
                        && header[0] == (byte) 0x89 && header[1] == 0x50 && header[2] == 0x4e && header[3] == 0x47
                        && header[4] == 0x0d && header[5] == 0x0a && header[6] == 0x1a && header[7] == 0x0a;
                case "image/webp" -> header.length >= 12
                        && header[0] == 0x52 && header[1] == 0x49 && header[2] == 0x46 && header[3] == 0x46
                        && header[8] == 0x57 && header[9] == 0x45 && header[10] == 0x42 && header[11] == 0x50;
                default -> false;
            };
        } catch (IOException exception) {
            return false;
        }
    }

    private Path resolve(String filename) throws IOException {
        if (filename == null || filename.isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Member photo not found.");
        }
        Path resolved = storageDirectory.resolve(filename).normalize();
        if (!resolved.startsWith(storageDirectory)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid member photo reference.");
        }
        return resolved;
    }
}
