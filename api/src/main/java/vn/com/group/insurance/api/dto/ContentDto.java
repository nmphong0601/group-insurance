package vn.com.group.insurance.api.dto;

import lombok.Data;
import vn.com.group.insurance.api.annotation.ExcelCollection;
import vn.com.group.insurance.api.annotation.ExcelNodeName;

import java.util.ArrayList;
import java.util.List;

@Data
public class ContentDto {
    @ExcelNodeName
    private String contentName;

    @ExcelCollection(keyword = {"Quyền lợi", "Benefit"}, level = 4, itemClass = BenefitDto.class)
    private List<BenefitDto> benefits = new ArrayList<>();
}
