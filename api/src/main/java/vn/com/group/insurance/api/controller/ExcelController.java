package vn.com.group.insurance.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.com.group.insurance.api.dto.*;
import vn.com.group.insurance.api.mapper.ExcelTreeMapper;
import vn.com.group.insurance.api.service.ExcelService;

import java.util.List;
import java.util.Map;

/**
 * REST Controller exposing API microservice endpoints.
 */
@RestController
@RequestMapping("/api/v1/excel")
public class ExcelController {

    private final ExcelService excelService;

    public ExcelController(ExcelService excelService) {
        this.excelService = excelService;
    }

//    @PostMapping("/upload")
//    public ResponseEntity<?> uploadExcel(@RequestParam("file") MultipartFile file) {
//        if (file.isEmpty()) {
//            return ResponseEntity.badRequest().body("Please upload an Excel file.");
//        }
//
//        try {
//            List<Map<String, String>> result = excelService.readDynamicExcel(file);
//            return ResponseEntity.ok(result);
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError().body("Failed to read Excel file: " + e.getMessage());
//        }
//    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadExcelFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File không được để trống!");
        }
        try {
            List<PackageDto> packages = excelService.parseExcelDynamic(file);
            return ResponseEntity.ok(packages);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Lỗi xử lý file: " + e.getMessage());
        }
    }

    @PostMapping("/upload-dynamic")
    public ResponseEntity<?> uploadExcelFileDynamic(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File không được để trống!");
        }
        try {
            List<PackageDto> result = excelService.parseExcelNDepth(file, new ExcelTreeMapper<>() {

                @Override
                public PackageDto createRootNode(String name) {
                    return new PackageDto(name); // Khởi tạo Level 0
                }

                @Override
                public Object createParentNode(int level, String name, Object value) {
                    // Dựa vào độ sâu để khởi tạo DTO tương ứng
                    return switch (level) {
                        case 1 -> new ProductDto(name, value);
                        case 2 -> new BenefitDto(name, value);
                        case 3 -> new BenefitDto(name, value);
                        // case 4: return new SubBenefitDto(); (Thêm tuỳ ý)
                        default -> null;
                    };
                }

                @Override
                public void linkNode(int level, Object childNode, Object parentNode) {
                    // Gắn con vào cha dựa vào độ sâu
                    switch (level) {
                        case 1:
                            ((PackageDto) parentNode).getProducts().add((ProductDto) childNode);
                            break;
                        case 2:
                            ((ProductDto) parentNode).getMainBenefits().add((BenefitDto) childNode);
                            break;
                        case 3:
                            ((BenefitDto) parentNode).getChildBenefits().add((BenefitDto) childNode);
                            break;
                    }
                }

                @Override
                public void processLeaf(int level, Object parentNode, String name, Object value) {
                    // Phân loại xử lý các Node không có con
                    switch (level) {
                        case 1:
                            // Các Node lá cấp 1 (như Nhóm nhân viên, Nhóm nghề) -> Gán vào field của Package
                            ((PackageDto) parentNode).getPackageAttributes().put(name, value);
                            break;
                        case 2:
                            // Các Node lá cấp 2 (như Lương trung bình) -> Gán vào field của Product
                            ((ProductDto) parentNode).getProductAttributes().put(name, value);
                            break;
                        case 3:
                            // Nếu ở cấp 3 mà phát hiện Node lá, có thể sinh ra Benefit Object và gài vô luôn
                            BenefitDto b = new BenefitDto(name, value);
                            ((BenefitDto) parentNode).getChildBenefits().add(b);
                            break;
                        case 4:
                            // Nếu file có level sâu hơn... xử lý tương tự
                            break;
                    }
                }
            });

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Lỗi: " + e.getMessage());
        }
    }
}
