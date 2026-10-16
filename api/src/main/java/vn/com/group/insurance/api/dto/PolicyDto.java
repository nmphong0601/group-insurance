package vn.com.group.insurance.api.dto;

import lombok.Data;
import vn.com.group.insurance.api.annotation.ExcelProperty;

import java.util.ArrayList;
import java.util.List;

@Data
public class PolicyDto {
    @ExcelProperty("Tên công ty")
    private String companyName;

    @ExcelProperty("Tên công ty bán")
    private String sellingCompanyName;

    @ExcelProperty("Thời hạn hợp đồng")
    private String contractDuration;

    @ExcelProperty("Kênh buôn bán")
    private String salesChannel;

    @ExcelProperty("Tổng số tiền")
    private Long totalAmount;

    // Danh sách các Package
    private List<PackageDto> packages = new ArrayList<>();
}

@Data
public class PolicyDto {
    @ExcelProperty(keyword = {"Tên công ty"}, level = 0)
    private String companyName;

    @ExcelProperty(keyword = {"Tổng số tiền"}, level = 0)
    private Long totalAmount;

    private List<PackageDto> packages = new ArrayList<>(); // Quét ngang nên bỏ qua Collection
}

