package vn.com.group.insurance.api.dto;

import lombok.Data;
import vn.com.group.insurance.api.annotation.ExcelCollection;
import vn.com.group.insurance.api.annotation.ExcelNodeName;
import vn.com.group.insurance.api.annotation.ExcelProperty;

import java.util.ArrayList;
import java.util.List;

@Data
public class PackageDto {
    @ExcelNodeName
    private String packageName;

    @ExcelProperty(keyword = "Nhóm nhân viên", level = 1)
    private String groupInsurance;

    @ExcelProperty(keyword = "Số lượng nhân vên", level = 1)
    private String totalPerson;

    @ExcelProperty(keyword = "Nhóm nghề", level = 1)
    private String occupationClass;

    // Nếu dòng Excel chứa chữ "Sản phẩm", đẻ ra ProductDto (Level 2) và tống vào List này
    @ExcelCollection(keyword = {"Sản phẩm", "Product"}, level = 2, itemClass = ProductDto.class)
    private List<ProductDto> products = new ArrayList<>();
}
