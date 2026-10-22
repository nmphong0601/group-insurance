package com.example.excelapi.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public abstract class CommonTreeExcelDto {
    
    // 1. Trạng thái và Ghi nhận lỗi cho Node hiện tại
    private boolean isValid = true;
    private List<String> nodeErrors = new ArrayList<>();
    private Map<String, List<String>> fieldErrors = new LinkedHashMap<>();

    // ==========================================
    // 2. DỮ LIỆU ĐỘNG CỦA NODE (DYNAMIC PROPERTIES)
    // ==========================================
    // Chứa các thuộc tính không được khai báo trong Class nhưng vẫn xuất hiện trên file Excel
    private Map<String, Object> dynamicProperties = new LinkedHashMap<>();

    public void addFieldError(String fieldName, String errorMsg) {
        this.fieldErrors.computeIfAbsent(fieldName, k -> new ArrayList<>()).add(errorMsg);
        this.isValid = false;
    }

    public void addNodeError(String errorMsg) {
        this.nodeErrors.add(errorMsg);
        this.isValid = false;
    }
}
