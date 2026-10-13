package vn.com.group.insurance.api.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class PackageDto {
    private String packageName;       // Tên Gói (Gói_01, Gói_02,...)
    private String employeeGroup;     // Nhóm nhân viên
    private Integer employeeCount;    // Số lượng nhân viên
    private String occupationalGroup; // Nhóm nghề

    private List<ProductDto> products = new ArrayList<>(); // Danh sách Sản phẩm

    private Long totalAmount;         // Tổng số tiền
    private Long averageAmount;       // Số tiền trung bình
}