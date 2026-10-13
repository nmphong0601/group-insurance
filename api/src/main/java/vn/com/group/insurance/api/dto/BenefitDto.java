package vn.com.group.insurance.api.dto;

import lombok.Data;

@Data
public class BenefitDto {
    private String benefitName; // Tên quyền lợi (Quyền lợi 1, Quyền lợi 2...)
    private Boolean isCheck;    // True/False
}