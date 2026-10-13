package vn.com.group.insurance.api.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ContentDto {
    private String contentName; // Tên nội dung (Ví dụ: Gói Sản phẩm 2_Nội dung 1)
    private String contentValue; // Giá trị của nội dung (Ví dụ: 240000000)

    // Danh sách quyền lợi thuộc về nội dung này
    private List<BenefitDto> benefits = new ArrayList<>();
}
