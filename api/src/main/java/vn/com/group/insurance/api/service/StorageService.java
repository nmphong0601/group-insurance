package com.example.filemodule.service;

import com.example.filemodule.config.StorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;

@Service
public class StorageService {

    private final Path rootLocation;

    @Autowired
    public StorageService(StorageProperties properties) {
        // Chuẩn hóa Root Path thành đường dẫn tuyệt đối
        this.rootLocation = Paths.get(properties.getRootPath()).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(this.rootLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Không thể khởi tạo thư mục gốc: " + this.rootLocation, ex);
        }
    }

    /**
     * TÍNH NĂNG 1: UPLOAD FILE KÈM SUB-PATH
     * 
     * @param file File tải lên
     * @param subPath Thư mục con (VD: "excel/imports/2026", "avatars", "invoices")
     * @return Đường dẫn tương đối (Relative path) để lưu vào Database
     */
    public String storeFile(MultipartFile file, String subPath) {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Chống lỗ hổng Path Traversal (VD: hacker gửi tên file là "../../../etc/passwd")
            if (originalFilename.contains("..")) {
                throw new RuntimeException("Tên file chứa ký tự không hợp lệ: " + originalFilename);
            }

            // Tạo thư mục đích (Tự động tạo các thư mục cha nếu chưa tồn tại)
            Path targetDirectory = this.rootLocation;
            if (subPath != null && !subPath.trim().isEmpty()) {
                // Làm sạch subPath chống Path Traversal
                String cleanSubPath = StringUtils.cleanPath(subPath);
                targetDirectory = this.rootLocation.resolve(cleanSubPath).normalize();
                
                // Đảm bảo subPath không nhảy ra khỏi rootLocation
                if (!targetDirectory.startsWith(this.rootLocation)) {
                     throw new RuntimeException("Sub-path không được phép thoát khỏi Root directory!");
                }
                Files.createDirectories(targetDirectory);
            }

            // Đổi tên file (Thêm timestamp để chống trùng)
            String uniqueFilename = System.currentTimeMillis() + "_" + originalFilename;
            Path targetFilePath = targetDirectory.resolve(uniqueFilename);

            // Copy file vào ổ cứng
            Files.copy(file.getInputStream(), targetFilePath, StandardCopyOption.REPLACE_EXISTING);

            // Trả về đường dẫn tương đối (VD: "excel/imports/1699999_data.xlsx")
            if (subPath != null && !subPath.trim().isEmpty()) {
                return Paths.get(subPath).resolve(uniqueFilename).toString().replace("\\", "/");
            }
            return uniqueFilename;

        } catch (IOException ex) {
            throw new RuntimeException("Lỗi khi lưu file " + originalFilename, ex);
        }
    }

    /**
     * TÍNH NĂNG 2: TẢI FILE TỪ NHIỀU NGUỒN KHÁC NHAU
     * 
     * @param fileLocation Đường dẫn file cần đọc
     * @return Đối tượng Resource để Spring truyền xuống Client hoặc đọc luồng
     */
    public Resource loadFileAsResource(String fileLocation) {
        try {
            Resource resource;

            // Nguồn 1: File nằm trong mã nguồn (Resources/Classpath) - VD: file Excel mẫu
            if (fileLocation.startsWith("classpath:")) {
                String pathWithinResources = fileLocation.substring("classpath:".length());
                resource = new ClassPathResource(pathWithinResources);
            } 
            else {
                Path path = Paths.get(fileLocation);
                
                // Nguồn 2: Nếu là đường dẫn tuyệt đối (C:/..., hoặc /mnt/...) (External Server)
                if (path.isAbsolute()) {
                    resource = new UrlResource(path.toUri());
                } 
                // Nguồn 3: Nếu là đường dẫn tương đối, tự động nối với Root Path
                else {
                    Path resolvedPath = this.rootLocation.resolve(path).normalize();
                    resource = new UrlResource(resolvedPath.toUri());
                }
            }

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Không tìm thấy hoặc không thể đọc file: " + fileLocation);
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi nạp file: " + fileLocation, e);
        }
    }
}
