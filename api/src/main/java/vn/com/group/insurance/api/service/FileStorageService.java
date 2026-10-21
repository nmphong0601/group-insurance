package vn.com.group.insurance.api.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import vn.com.group.insurance.api.config.FileStorageProperties;

import java.io.IOException;
import java.nio.file.*;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Không thể tạo thư mục lưu trữ file: " + this.fileStorageLocation, ex);
        }
    }

    /**
     * Lưu file xuống Server và trả về đường dẫn vật lý tuyệt đối
     */
    public Path storeFile(MultipartFile file) {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        
        try {
            if (originalFilename.contains("..")) {
                throw new RuntimeException("Tên file chứa ký tự đường dẫn không hợp lệ: " + originalFilename);
            }

            // Thêm timestamp để chống trùng tên file khi có nhiều người upload cùng lúc
            String fileName = System.currentTimeMillis() + "_" + originalFilename;
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            
            // Copy file vào ổ cứng (Ghi đè nếu đã tồn tại)
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return targetLocation;
        } catch (IOException ex) {
            throw new RuntimeException("Lỗi khi lưu file " + originalFilename + ". Vui lòng thử lại!", ex);
        }
    }
}
