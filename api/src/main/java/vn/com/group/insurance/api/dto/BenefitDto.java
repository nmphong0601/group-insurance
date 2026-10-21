package vn.com.group.insurance.api.dto;

import lombok.Data;
import vn.com.group.insurance.api.annotation.ExcelCollection;
import vn.com.group.insurance.api.annotation.ExcelNodeName;
import vn.com.group.insurance.api.annotation.ExcelNodeValue;

import java.util.ArrayList;
import java.util.List;

@Data
public class BenefitDto {
    @ExcelNodeName
    private String benefitName; // Tên quyền lợi (Quyền lợi 1, Quyền lợi 2...)
    @ExcelNodeValue
    private Object benefitValue;
    private Boolean isCheck;    // True/False

    @ExcelCollection(keyword = {"Sản phẩm", "Product"}, level = 4, itemClass = BenefitDto.class)
    private List<BenefitDto> childBenefits = new ArrayList<>();
}