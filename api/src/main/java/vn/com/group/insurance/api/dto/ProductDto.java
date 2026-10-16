package vn.com.group.insurance.api.dto;

import lombok.Data;
import vn.com.group.insurance.api.annotation.ExcelProperty;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//@Data
//public class ProductDto {
//    private String productName;               // Tên gói sản phẩm
//    private Boolean isCheck;                  // True/False
//    private Boolean calculateBasedOnSalary;   // Tính tiền dựa trên tiền lương
//    private Long averageMonthlySalary;        // Lương trung bình tháng
//    private Integer numberOfMonths;           // Số tháng
//
//    // Lưu các nội dung (trong mỗi nội dung có danh sách benefits riêng)
//    private List<ContentDto> contents = new ArrayList<>();
//
//    // Danh sách quyền lợi chung (Dành cho các gói như Gói 1 không có Nội dung con)
//    private List<BenefitDto> generalBenefits = new ArrayList<>();
//}

@Data
public class ProductDto {
    private String productName;
    private Boolean isCheck;

    @ExcelProperty("Tính Tiền dựa trên lương") // Khớp tự động dù Excel ghi "Gói 1_Tính Tiền..." hay "Gói 2_Tính Tiền..."
    private Boolean calculateBasedOnSalary;

    @ExcelProperty("Lương trung bình tháng")
    private Long averageMonthlySalary;

    // Lưu mọi thuộc tính con linh hoạt (Tính tiền, số tháng, lương, v.v.)
    private Map<String, Object> productAttributes = new LinkedHashMap<>();

    private List<BenefitDto> mainBenefits = new ArrayList<>();

    public ProductDto() {
    }

    public ProductDto(String name, Object value) {
        productName = name;
    }
}

@Data
public class ProductDto {
    @ExcelNodeName
    private String productName;

    @ExcelNodeValue
    private Boolean isCheck; // Tự động hứng giá trị TRUE/FALSE của dòng "Gói Sản phẩm 1"

    @ExcelProperty(keyword = {"Tính tiền", "dựa trên lương"}, level = 2)
    private Boolean calculateBasedOnSalary;

    @ExcelProperty(keyword = {"Lương trung bình"}, level = 2)
    private Long averageMonthlySalary;

    // Nếu dòng Excel có chữ "Nội dung" hoặc "Quyền lợi chính", đẻ ra ContentDto (Level 3)
    @ExcelCollection(keyword = {"Nội dung", "Quyền lợi chính"}, level = 3, itemClass = ContentDto.class)
    private List<ContentDto> contents = new ArrayList<>();
}

