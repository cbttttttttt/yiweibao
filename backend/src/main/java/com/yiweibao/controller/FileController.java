package com.yiweibao.controller;

import com.yiweibao.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class FileController {

    private final Path uploadDir;

    public FileController(@Value("${app.upload.path}") String uploadPath) {
        this.uploadDir = Path.of(uploadPath).toAbsolutePath();
    }

    @PostMapping("/upload")
    public ApiResponse<String> upload(@RequestParam("file") MultipartFile file) {
        try {
            String originalName = file.getOriginalFilename();
            String ext = originalName != null && originalName.contains(".")
                    ? originalName.substring(originalName.lastIndexOf(".")) : "";
            String filename = UUID.randomUUID().toString() + ext;
            file.transferTo(uploadDir.resolve(filename).toFile());
            return ApiResponse.success("/api/files/" + filename);
        } catch (IOException e) {
            return ApiResponse.error(500, "文件上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/files/**")
    public ResponseEntity<Resource> serveFile(@RequestParam(value = "path", required = false) String altPath,
                                              jakarta.servlet.http.HttpServletRequest request) {
        try {
            String relativePath;
            if (altPath != null) {
                relativePath = altPath;
            } else {
                relativePath = request.getRequestURI().substring("/api/files/".length());
            }
            Path filePath = uploadDir.resolve(relativePath).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                String filename = filePath.getFileName().toString().toLowerCase();
                MediaType mediaType;
                if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
                    mediaType = MediaType.IMAGE_JPEG;
                } else if (filename.endsWith(".png")) {
                    mediaType = MediaType.IMAGE_PNG;
                } else if (filename.endsWith(".webp")) {
                    mediaType = MediaType.parseMediaType("image/webp");
                } else if (filename.endsWith(".gif")) {
                    mediaType = MediaType.IMAGE_GIF;
                } else {
                    mediaType = MediaType.APPLICATION_OCTET_STREAM;
                }
                return ResponseEntity.ok()
                        .contentType(mediaType)
                        .body(resource);
            }
            return ResponseEntity.notFound().build();
        } catch (MalformedURLException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
