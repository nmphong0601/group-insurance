package vn.com.group.insurance.api.dto;

import lombok.Data;
import vn.com.group.insurance.api.annotation.ExcelProperty;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//@Data
//public class PackageDto {
//    private String packageName;       // Tên Gói (Gói_01, Gói_02,...)
//    private String employeeGroup;     // Nhóm nhân viên
//    private Integer employeeCount;    // Số lượng nhân viên
//    private String occupationalGroup; // Nhóm nghề
//
//    private List<ProductDto> products = new ArrayList<>(); // Danh sách Sản phẩm
//
//    private Long totalAmount;         // Tổng số tiền
//    private Long averageAmount;       // Số tiền trung bình
//}

@Data
public class PackageDto {
    @ExcelProperty("Gói")
    private String packageName;

    @ExcelProperty("Nhóm nhân viên")
    private String groupInsurance;

    @ExcelProperty("Số lượng nhân vên")
    private String totalPerson;

    @ExcelProperty("Nhóm nghề")
    private String occupationClass;

    // Lưu các thuộc tính ngang hàng của Gói (Nhóm NV, Tổng tiền, v.v.)
    private Map<String, Object> packageAttributes = new LinkedHashMap<>();

    private List<ProductDto> products = new ArrayList<>();

    public PackageDto() {
    }

    public PackageDto(String name) {
        packageName = name;
    }
}