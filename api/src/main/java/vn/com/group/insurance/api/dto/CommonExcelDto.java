package vn.com.group.insurance.api.dto;

import lombok.Data;
import vn.com.group.insurance.api.annotation.ExcelProperty;

import java.util.ArrayList;
import java.util.List;

@Data
public abstract class CommonExcelDto {

    // ==========================================
    // 1. NHÓM DỮ LIỆU HỆ THỐNG TỰ BƠM (Không cần cột Excel)
    // ==========================================
    private int excelRowNumber; // Vị trí dòng vật lý trên file Excel (Dùng để báo lỗi)
    private List<String> importErrors = new ArrayList<>(); // Danh sách lỗi của dòng này
    private boolean isValid = true; // Trạng thái hợp lệ (Mặc định là true)

    // ==========================================
    // 2. NHÓM DỮ LIỆU QUÉT TỪ CỘT EXCEL (Nếu có)
    // ==========================================
    @ExcelProperty(keyword = {"Ghi chú", "Note"}, level = 0)
    private String note;

    // Tiện ích ghi log lỗi dễ dàng
    public void addError(String errorMsg) {
        this.importErrors.add(errorMsg);
        this.isValid = false;
    }
}
