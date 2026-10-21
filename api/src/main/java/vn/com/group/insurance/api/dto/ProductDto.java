package vn.com.group.insurance.api.dto;

import lombok.Data;
import vn.com.group.insurance.api.annotation.ExcelCollection;
import vn.com.group.insurance.api.annotation.ExcelNodeName;
import vn.com.group.insurance.api.annotation.ExcelNodeValue;
import vn.com.group.insurance.api.annotation.ExcelProperty;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    // Nếu dòng Excel có chữ "Nội dung" hoặc "Quyền lợi chính", đẻ ra BenefitDto (Level 3)
    @ExcelCollection(keyword = {"Nội dung", "Quyền lợi chính"}, level = 3, itemClass = BenefitDto.class)
    private List<BenefitDto> mainBenefits = new ArrayList<>();
}

