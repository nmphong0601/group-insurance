package vn.com.group.insurance.api.dto;

import lombok.Data;
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

    // Lưu mọi thuộc tính con linh hoạt (Tính tiền, số tháng, lương, v.v.)
    private Map<String, Object> productAttributes = new LinkedHashMap<>();

    private List<BenefitDto> mainBenefits = new ArrayList<>();

    public ProductDto() {
    }

    public ProductDto(String name, Object value) {
        productName = name;
    }
}
