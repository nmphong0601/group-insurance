package vn.com.group.insurance.api.dto;

import lombok.Data;
import vn.com.group.insurance.api.annotation.ExcelProperty;

import java.util.ArrayList;
import java.util.List;

@Data
public class PolicyDto {

    @ExcelProperty(keyword = {"Tên công ty"}, level = 0)
    private String companyName;

    @ExcelProperty(keyword = {"Tên công ty bán"}, level = 0)
    private String sellingCompanyName;

    @ExcelProperty(keyword = {"Thời hạn hợp đồng"}, level = 0)
    private String contractDuration;

    @ExcelProperty(keyword = {"Kênh buôn bán"}, level = 0)
    private String salesChannel;

    @ExcelProperty(keyword = {"Tổng số tiền"}, level = 0)
    private Long totalAmount;

    private List<PackageDto> packages = new ArrayList<>(); // Quét ngang nên bỏ qua Collection
}

