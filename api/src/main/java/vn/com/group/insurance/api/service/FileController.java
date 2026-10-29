package com.example.excelapi.controller;

import com.example.filemodule.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private StorageService storageService;

    // 1. API UPLOAD (Hỗ trợ sub-path động)
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "subPath", required = false, defaultValue = "misc") String subPath) {
        try {
            // Upload và lấy relative path lưu Database
            String relativePath = storageService.storeFile(file, subPath);
            return ResponseEntity.ok("File đã lưu tại: " + relativePath);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // 2. API DOWNLOAD (Tải file về trình duyệt)
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam("filePath") String filePath, HttpServletRequest request) {
        
        Resource resource = storageService.loadFileAsResource(filePath);

        // Xác định loại MIME (Content-Type) của file để trình duyệt biết cách xử lý
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (Exception ex) {
            // Log lỗi nếu đọc classpath resource không ra file vật lý (Bình thường)
        }
        if (contentType == null) {
            contentType = "application/octet-stream"; // Mặc định là file nhị phân tải về
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                // Dòng này lệnh cho trình duyệt tải file xuống thay vì mở trực tiếp
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
