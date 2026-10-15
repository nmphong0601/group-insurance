package vn.com.group.insurance.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.com.group.insurance.api.dto.*;
import vn.com.group.insurance.api.mapper.ExcelTreeMapper;
import vn.com.group.insurance.api.service.ExcelService;
import vn.com.group.insurance.api.utils.ExcelReflectionUtils;

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
            ExcelTreeMapper<PackageDto> packageTreeMapper = new ExcelTreeMapper<>() {

                // --- HÀM TIỆN ÍCH: Ép kiểu an toàn ra Boolean ---
                // Xử lý bao quát cả trường hợp Excel lưu dạng Số (1/0), Chữ (TRUE/FALSE/YES) hay Boolean gốc
                private Boolean extractIsCheck(Object value) {
                    if (value == null) return false;
                    if (value instanceof Boolean) return (Boolean) value;
                    if (value instanceof Number) return ((Number) value).intValue() == 1; // 1 = true, 0 = false

                    String s = value.toString().trim().toUpperCase();
                    return s.equals("1") || s.equals("TRUE") || s.equals("YES");
                }

                @Override
                public PackageDto createRootNode(String name) {
                    return new PackageDto(name); // Khởi tạo Level 0
                }

                @Override
                public Object createParentNode(int level, String name, Object value) {
                    // Dựa vào độ sâu để khởi tạo DTO tương ứng
                    return switch (level) {
                        case 1:
                            ProductDto p = new ProductDto();
                            p.setProductName(name);
                            p.setIsCheck(extractIsCheck(value));
                            yield p;
                        case 2:
                            BenefitDto mainBenefit = new BenefitDto();
                            mainBenefit.setBenefitName(name);
                            mainBenefit.setIsCheck(extractIsCheck(value));;
                            yield mainBenefit;
                        case 3:
                            BenefitDto benefit = new BenefitDto();
                            benefit.setBenefitName(name);
                            benefit.setIsCheck(extractIsCheck(value));;
                            yield benefit;
                            // case 4: return new SubBenefitDto(); (Thêm tuỳ ý)
                        default:
                            yield null;
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
                    // Nếu là Benefit ở dạng Node Lá (Là dòng Quyền lợi phụ không có nhánh con bên trong nữa)
                    if (level == 3 && parentNode instanceof BenefitDto) {
                        BenefitDto childBenefit = new BenefitDto();
                        childBenefit.setBenefitName(name);
                        // MAP TRỰC TIẾP GIÁ TRỊ VÀO isCheck CHO QUYỀN LỢI LÁ
                        childBenefit.setIsCheck(extractIsCheck(value));

                        ((BenefitDto) parentNode).getChildBenefits().add(childBenefit);
                        return; // return luôn để không đưa chuỗi này qua ExcelReflectionUtils quét nữa
                    }

                    // Nếu không phải quyền lợi, đẩy qua Reflection để tìm Annotation như bình thường
                    ExcelReflectionUtils.injectValue(parentNode, name, value);
                }
            };

            // List<PackageDto> result = excelService.parseExcelNDepth(file, packageTreeMapper);

            // Tham số thứ 3 (số 6) chính là vị trí của dòng chứa tiêu đề "Gói_01", "Gói_02"
            // (Tính từ 0, ví dụ nếu ở Excel là dòng số 7 thì index = 6)
            int packageHeaderRowIndex = 7;
            PolicyDto result = excelService.parseExcelWithPolicy(file, packageTreeMapper, packageHeaderRowIndex);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Lỗi: " + e.getMessage());
        }
    }
}
