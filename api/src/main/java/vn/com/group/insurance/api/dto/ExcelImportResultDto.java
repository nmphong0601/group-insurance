package com.example.excelapi.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ExcelImportResultDto<T> {
    
    // Các thông số thống kê toàn file
    private int totalRows;
    private int validRows;
    private int errorRows;
    private ParseStatus parseStatus;

    // Phân loại dữ liệu
    private List<T> validData = new ArrayList<>();
    private List<T> errorData = new ArrayList<>();

    // Enum định nghĩa trạng thái Parse
    public enum ParseStatus {
        SUCCESS,           // 100% dòng hợp lệ
        PARTIAL_SUCCESS,   // Có dòng hợp lệ, có dòng lỗi
        FAILED             // 100% dòng lỗi hoặc file rỗng/hỏng
    }

    /**
     * Hàm tiện ích: Tự động tính toán các chỉ số sau khi đã phân loại xong dữ liệu
     */
    public void calculateSummary() {
        this.validRows = this.validData.size();
        this.errorRows = this.errorData.size();
        this.totalRows = this.validRows + this.errorRows;

        if (this.totalRows == 0 || this.validRows == 0) {
            this.parseStatus = ParseStatus.FAILED;
        } else if (this.errorRows > 0) {
            this.parseStatus = ParseStatus.PARTIAL_SUCCESS;
        } else {
            this.parseStatus = ParseStatus.SUCCESS;
        }
    }
}
