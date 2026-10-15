package vn.com.group.insurance.api.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BenefitDto {
    private String benefitName; // Tên quyền lợi (Quyền lợi 1, Quyền lợi 2...)
    private Object benefitValue;
    private Boolean isCheck;    // True/False

    private List<BenefitDto> childBenefits = new ArrayList<>();

    public BenefitDto() {
    }

    public BenefitDto(String name, Object value) {
        benefitName = name;
        benefitValue = value;
    }
}