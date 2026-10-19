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

@PostMapping("/upload")
public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
    try {
        int packageHeaderRowIndex = 6; 

        PolicyDto result = excelService.parseExcelWithPolicy(file, new ExcelTreeMapper<PackageDto>() {

            private Boolean extractIsCheck(Object value) {
                if (value == null) return false;
                if (value instanceof Boolean) return (Boolean) value;
                if (value instanceof Number) return ((Number) value).intValue() == 1;
                String s = value.toString().trim().toUpperCase();
                return s.equals("1") || s.equals("TRUE") || s.equals("YES");
            }

            @Override
            public PackageDto createRootNode(String name) {
                return new PackageDto(name); 
            }

            @Override
            public Object createParentNode(Object parentNode, String name, Object value) {
                // LOGIC NỘI SUY (Đỉnh cao của OOP): Kiểm tra Instance của cha để đẻ ra con
                if (parentNode instanceof PackageDto) {
                    ProductDto p = new ProductDto();
                    p.setProductName(name);
                    p.setIsCheck(extractIsCheck(value)); // Set TRUE/FALSE từ gốc
                    return p;
                } else if (parentNode instanceof ProductDto) {
                    ContentDto c = new ContentDto();
                    c.setContentName(name);
                    return c;
                } else if (parentNode instanceof ContentDto) {
                    BenefitDto b = new BenefitDto();
                    b.setBenefitName(name);
                    b.setIsCheck(extractIsCheck(value)); // Set TRUE/FALSE từ gốc
                    return b;
                }
                return null;
            }

            @Override
            public void linkNode(Object child, Object parent) {
                if (parent instanceof PackageDto && child instanceof ProductDto) {
                    ((PackageDto) parent).getProducts().add((ProductDto) child);
                } else if (parent instanceof ProductDto && child instanceof ContentDto) {
                    ((ProductDto) parent).getContents().add((ContentDto) child);
                } else if (parent instanceof ContentDto && child instanceof BenefitDto) {
                    ((ContentDto) parent).getBenefits().add((BenefitDto) child);
                }
            }

            @Override
            public void processLeaf(Object parentNode, String name, Object value) {
                // Đặc biệt: Nếu là Node lá ở cấp độ Quyền Lợi (Cha là Content)
                // Nó sinh ra một Object rời thay vì Field thuộc tính
                if (parentNode instanceof ContentDto) {
                    BenefitDto b = new BenefitDto();
                    b.setBenefitName(name);
                    b.setIsCheck(extractIsCheck(value)); 
                    ((ContentDto) parentNode).getBenefits().add(b);
                    return; 
                }
                
                // Các Node lá còn lại: Bắn thẳng vào Field thông qua Annotation Reflection (Bất kể prefix là gì)
                ExcelReflectionUtils.injectValue(parentNode, name, value);
            }

        }, packageHeaderRowIndex);

        return ResponseEntity.ok(result);
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.internalServerError().body("Lỗi: " + e.getMessage());
    }
}




@RestController
@RequestMapping("/api/excel")
public class ExcelUploadController {

    @Autowired
    private ExcelObjectMappingService excelService;

    // Giả lập một Object lấy từ DB
    @Data
    class ConfigRule {
        private String targetField; // Tên biến trong Java
        private List<String> matchKeywords; // Danh sách từ khóa
        
        public ConfigRule(String targetField, List<String> matchKeywords) {
            this.targetField = targetField;
            this.matchKeywords = matchKeywords;
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        try {
            // Bước 1: Fetch danh sách cấu hình từ Database/API
            List<ConfigRule> rulesFromDb = Arrays.asList(
                new ConfigRule("companyName", Arrays.asList("Tên công ty", "Đơn vị chủ quản")),
                new ConfigRule("calculateBasedOnSalary", Arrays.asList("Tính tiền", "dựa trên lương")),
                new ConfigRule("averageMonthlySalary", Arrays.asList("Lương trung bình", "Mức lương")),
                new ConfigRule("products", Arrays.asList("Sản phẩm", "Product")),
                new ConfigRule("contents", Arrays.asList("Nội dung", "Quyền lợi chính")),
                new ConfigRule("benefits", Arrays.asList("Quyền lợi", "Phụ cấp"))
            );

            // Bước 2: Build Từ điển (Map) từ List Object
            Map<String, List<String>> dynamicDictionary = new HashMap<>();
            for (ConfigRule rule : rulesFromDb) {
                dynamicDictionary.put(rule.getTargetField(), rule.getMatchKeywords());
            }

            // Bước 3: Truyền file và Từ điển vào Engine xử lý
            int packageHeaderRowIndex = 6; 
            PolicyDto result = excelService.parseExcelByDictionary(
                    file, 
                    packageHeaderRowIndex, 
                    dynamicDictionary
            );
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Lỗi: " + e.getMessage());
        }
    }
}

@PostMapping("/upload")
public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
    try {
        // Cấu hình từ Database (Hoặc Admin Dashboard)
        Map<String, FieldRule> dynamicDictionary = new HashMap<>();

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
        PolicyDto result = excelService.parseExcelByDictionary(file, 6, dynamicDictionary);
        return ResponseEntity.ok(result);

    } catch (Exception e) {
        return ResponseEntity.internalServerError().body("Lỗi: " + e.getMessage());
    }
}


