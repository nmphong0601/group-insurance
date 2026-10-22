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

package com.example.excelapi.dto;

import com.example.excelapi.annotation.ExcelProperty;
import lombok.Data;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public abstract class CommonExcelDto {
    
    private int excelRowNumber; // Vị trí dòng trên Excel
    private boolean isValid = true; 

    // Lỗi cấp độ toàn cục của dòng (Ví dụ: Dòng thiếu dữ liệu bắt buộc để xử lý)
    private List<String> globalErrors = new ArrayList<>();

    // Lỗi cấp độ Thuộc tính (Key: Tên biến, Value: Danh sách lỗi)
    private Map<String, List<String>> fieldErrors = new LinkedHashMap<>();

    @ExcelProperty({"Ghi chú", "Note"})
    private String note;

    /**
     * Ghi nhận lỗi cho một Field cụ thể
     */
    public void addFieldError(String fieldName, String errorMsg) {
        this.fieldErrors.computeIfAbsent(fieldName, k -> new ArrayList<>()).add(errorMsg);
        this.isValid = false;
    }

    /**
     * Ghi nhận lỗi chung cho cả dòng
     */
    public void addGlobalError(String errorMsg) {
        this.globalErrors.add(errorMsg);
        this.isValid = false;
    }
}

