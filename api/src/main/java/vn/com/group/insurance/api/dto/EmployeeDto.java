package vn.com.group.insurance.api.dto;

import lombok.Data;
import vn.com.group.insurance.api.annotation.ExcelProperty;

@Data
public class EmployeeDto extends CommonExcelDto {

    @ExcelProperty(keyword = {"Mã NV", "Mã nhân viên", "ID"}, level = 0)
    private String employeeId;

    @ExcelProperty(keyword = {"Họ và tên", "Tên nhân viên"}, level = 0)
    private String fullName;

    @ExcelProperty(keyword = {"Lương", "Mức lương"}, level = 0)
    private Long salary;

    @ExcelProperty(keyword = {"Trạng thái", "Đang làm việc", "Active"}, level = 0)
    private Boolean isActive;

    @ExcelProperty(keyword = {"Email", "Thư điện tử"}, level = 0)
    private String email;
}
