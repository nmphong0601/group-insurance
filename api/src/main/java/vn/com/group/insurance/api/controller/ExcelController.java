package vn.com.group.insurance.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.com.group.insurance.api.dto.*;
import vn.com.group.insurance.api.entity.sqlserver.ExcelRuleEntity;
import vn.com.group.insurance.api.mapper.ExcelTreeMapper;
import vn.com.group.insurance.api.service.ExcelListMappingService;
import vn.com.group.insurance.api.service.ExcelObjectMappingService;
import vn.com.group.insurance.api.service.ExcelService;
import vn.com.group.insurance.api.service.FileStorageService;
import vn.com.group.insurance.api.utils.ExcelReflectionUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

/**
 * REST Controller exposing API microservice endpoints.
 */
@RestController
@RequestMapping("/api/v1/excel")
public class ExcelController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ExcelService excelService;

    @Autowired
    private ExcelObjectMappingService excelObjectMappingService;

    @Autowired
    private ExcelListMappingService excelListMappingService;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        try {
            // 1. Lưu file xuống Server trước
            Path savedFilePath = fileStorageService.storeFile(file);

            // Cấu hình từ Database (Hoặc Admin Dashboard)
            Map<String, FieldRule> dynamicDictionary = new LinkedHashMap<>();

            // Ví dụ 1: Map theo Index chính xác
            // Tên công ty luôn nằm ở Dòng số 0 (Row Index = 0). Quét theo ROW. Trả về String.
            dynamicDictionary.put("companyName", FieldRule.builder()
                    .index(0)
                    .level(0)
                    .direction(FieldRule.Direction.ROW)
                    .dataType(String.class)
                    .build());

            // Ví dụ 2: Map theo Name tương đối
            // Lương trung bình nằm ở dòng nào không rõ, nhưng chứa chữ "Lương trung bình". Trả về Long.
            dynamicDictionary.put("averageMonthlySalary", FieldRule.builder()
                    .name("Lương trung bình")
                    .level(2)
                    .direction(FieldRule.Direction.ROW)
                    .dataType(Long.class)
                    .build());

            // Ví dụ 3: Map theo Hướng COLUMN
            // Bạn muốn quét tên của Gói (Package 1, Package 2) nằm trên Header Cột (Ví dụ cột có chữ "Gói")
            dynamicDictionary.put("packageName", FieldRule.builder()
                    .name("Gói")
                    .level(1)
                    .direction(FieldRule.Direction.COLUMN)
                    .dataType(String.class)
                    .build());

            // Ví dụ 4: Khởi tạo Collection (Danh sách Sản phẩm)
            dynamicDictionary.put("products", FieldRule.builder()
                    .name("Sản phẩm")
                    .level(2)
                    .direction(FieldRule.Direction.ROW)
                    .build());

            // Chạy Engine
            PolicyDto result = excelObjectMappingService.parseExcelByDictionary(file, 6, dynamicDictionary);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Lỗi: " + e.getMessage());
        }
    }

    @PostMapping("/upload-dynamic")
    public ResponseEntity<?> uploadExcelFileDynamic(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File không được để trống!");
        }
        try {
            // 1. Lưu file xuống Server trước
            Path savedFilePath = fileStorageService.storeFile(file);

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
                    PackageDto packageDto = new PackageDto();
                    packageDto.setPackageName(name);
                    return packageDto; // Khởi tạo Level 0
                }

                @Override
                public Object createParentNode(Object parentNode, String name, Object value) {
                    // Dựa vào độ sâu để khởi tạo DTO tương ứng
                    return parentNode;
                }

                @Override
                public void linkNode(Object childNode, Object parentNode) {
                    if (parentNode instanceof ProductDto) {

                    }
                }

                @Override
                public void processLeaf(Object parentNode, String name, Object value) {
                    // Nếu là Benefit ở dạng Node Lá (Là dòng Quyền lợi phụ không có nhánh con bên trong nữa)
                    if (parentNode instanceof BenefitDto) {
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

    @PostMapping("/upload-employees")
    public ResponseEntity<?> uploadEmployees(@RequestParam("file") MultipartFile file) {
        try {
            // 1. Lưu file xuống Server trước
            Path savedFilePath = fileStorageService.storeFile(file);

            File convFile = savedFilePath.toFile();
            file.transferTo(convFile);

            List<EmployeeDto> employees = excelListMappingService.parseExcelList(convFile, EmployeeDto.class, 0, "STT");

            List<EmployeeDto> validEmployees = new ArrayList<>();
            List<EmployeeDto> errorEmployees = new ArrayList<>();

            // Validate dữ liệu cực kỳ dễ dàng
            for (EmployeeDto emp : employees) {
                if (emp.getFullName() == null || emp.getFullName().isEmpty()) {
                    emp.addError("Tên nhân viên không được để trống.");
                }
                if (emp.getSalary() != null && emp.getSalary() < 0) {
                    emp.addError("Mức lương không được là số âm.");
                }

                if (emp.isValid()) {
                    validEmployees.add(emp);
                    // Thực hiện lưu Database...
                } else {
                    errorEmployees.add(emp);
                }
            }

            // Nếu có lỗi, trả thẳng danh sách lỗi về Frontend kèm số dòng
            if (!errorEmployees.isEmpty()) {
                return ResponseEntity.badRequest().body(errorEmployees);
            /* Kết quả JSON Frontend nhận được:
               {
                 "excelRowNumber": 4,
                 "employeeId": "NV02",
                 "importErrors": ["Mức lương không được là số âm."],
                 "isValid": false
               }
            */
            }

            return ResponseEntity.ok("Import thành công " + validEmployees.size() + " nhân viên.");

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi: " + e.getMessage());
        }
    }
}


