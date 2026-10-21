package com.example.excelapi.service;

import com.example.excelapi.annotation.ExcelProperty;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

@Service
public class ExcelListMappingService {

    /**
     * @param file: File vật lý lấy từ FileStorage
     * @param clazz: Class DTO đích (Ví dụ: EmployeeDto.class)
     * @param headerRowIndex: Vị trí dòng chứa Tiêu đề Cột (Tính từ 0)
     */
    public <T> List<T> parseExcelList(File file, Class<T> clazz, int headerRowIndex) throws Exception {
        List<T> resultList = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(headerRowIndex);
            
            if (headerRow == null) return resultList;

            // BƯỚC 1: LẬP BẢN ĐỒ CỘT (COLUMN MAPPING)
            // Ánh xạ: Vị trí Cột (Index) -> Thuộc tính Java (Field)
            Map<Integer, Field> columnIndexToFieldMap = new HashMap<>();
            
            for (Cell cell : headerRow) {
                String headerName = getCellValueAsString(cell).trim().toLowerCase();
                if (headerName.isEmpty()) continue;

                // Tìm Field nào trong Class có chứa từ khóa khớp với Tên cột này
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.isAnnotationPresent(ExcelProperty.class)) {
                        String[] keywords = field.getAnnotation(ExcelProperty.class).value();
                        for (String kw : keywords) {
                            if (headerName.contains(kw.toLowerCase())) {
                                field.setAccessible(true);
                                columnIndexToFieldMap.put(cell.getColumnIndex(), field);
                                break; // Chuyển sang Field tiếp theo để tối ưu
                            }
                        }
                    }
                }
            }

            // BƯỚC 2: QUÉT DỮ LIỆU TỪ DÒNG BÊN DƯỚI HEADER
            for (int rowIndex = headerRowIndex + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                // Khởi tạo 1 Object động (Ví dụ: new EmployeeDto())
                T instance = clazz.getDeclaredConstructor().newInstance();
                boolean isRowEmpty = true;

                // Dựa vào Bản đồ Cột, bốc thẳng Data nhét vào Field
                for (Map.Entry<Integer, Field> entry : columnIndexToFieldMap.entrySet()) {
                    int colIndex = entry.getKey();
                    Field field = entry.getValue();

                    Cell cell = row.getCell(colIndex);
                    Object cellValue = getCellValueAsObject(cell);

                    if (cellValue != null && !cellValue.toString().trim().isEmpty()) {
                        isRowEmpty = false; // Dòng này có chứa dữ liệu thực
                        Object castedValue = castValue(field.getType(), cellValue);
                        field.set(instance, castedValue);
                    }
                }

                // Nếu dòng không hoàn toàn trống, thêm vào List kết quả
                if (!isRowEmpty) {
                    resultList.add(instance);
                }
            }
        }
        return resultList;
    }

    // ==========================================
    // CÁC HÀM ÉP KIỂU & TIỀN XỬ LÝ DỮ LIỆU
    // ==========================================

    private Object castValue(Class<?> targetType, Object value) {
        if (value == null) return null;

        String s;
        if (value instanceof Number) {
            s = new BigDecimal(value.toString()).stripTrailingZeros().toPlainString();
        } else {
            s = value.toString().trim();
            if (s.toUpperCase().contains("E")) {
                try {
                    s = new BigDecimal(s).stripTrailingZeros().toPlainString();
                } catch (Exception ignored) {}
            }
        }
        if (s.isEmpty()) return null;

        if (targetType == Boolean.class || targetType == boolean.class) {
            String up = s.toUpperCase();
            return up.equals("1") || up.equals("TRUE") || up.equals("YES") || up.equals("Y");
        }

        try {
            if (targetType == Long.class || targetType == long.class) return new BigDecimal(s).longValue();
            if (targetType == Integer.class || targetType == int.class) return new BigDecimal(s).intValue();
            if (targetType == Double.class || targetType == double.class) return new BigDecimal(s).doubleValue();
        } catch (Exception e) {
            System.err.println("Cảnh báo: Không thể ép giá trị '" + s + "' sang " + targetType.getSimpleName());
            return null;
        }

        if (targetType == String.class) return s;
        
        return value;
    }

    private Object getCellValueAsObject(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC: return cell.getNumericCellValue(); // Trả về số gốc để castValue xử lý an toàn
            case BOOLEAN: return cell.getBooleanCellValue();
            default: return null;
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC: return BigDecimal.valueOf(cell.getNumericCellValue()).stripTrailingZeros().toPlainString();
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default: return "";
        }
    }
}
