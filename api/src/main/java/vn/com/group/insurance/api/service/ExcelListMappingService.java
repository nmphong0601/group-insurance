package vn.com.group.insurance.api.service;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import vn.com.group.insurance.api.annotation.ExcelProperty;
import vn.com.group.insurance.api.dto.CommonExcelDto;

import java.io.File;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

@Service
public class ExcelListMappingService {

    /**
     * @param file: File vật lý
     * @param clazz: Class DTO đích
     * @param headerRowIndex: Vị trí dòng chứa Tiêu đề Cột
     * @param primaryColumnName: Tên cột chính. NẾU CỘT NÀY TRỐNG SẼ DỪNG ĐỌC FILE. (Truyền null nếu không muốn dùng)
     */
    public <T> List<T> parseExcelList(File file, Class<T> clazz, int headerRowIndex, String primaryColumnName) throws Exception {
        List<T> resultList = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(headerRowIndex);
            
            if (headerRow == null) return resultList;

            Map<Integer, Field> columnIndexToFieldMap = new HashMap<>();
            
            // Biến lưu trữ vị trí của cột Primary (Mặc định là -1, tức là chưa tìm thấy)
            int primaryColumnIndex = -1; 
            
            List<Field> allFields = getAllFields(clazz);
            
            // ==========================================
            // BƯỚC 1: LẬP BẢN ĐỒ CỘT
            // ==========================================
            for (Cell cell : headerRow) {
                String headerName = getCellValueAsString(cell).trim().toLowerCase();
                if (headerName.isEmpty()) continue;

                // Xác định vị trí của cột Primary dựa vào tham số truyền vào
                if (primaryColumnName != null && headerName.contains(primaryColumnName.toLowerCase())) {
                    primaryColumnIndex = cell.getColumnIndex();
                }

                // Map các cột vào Field của Java Class
                for (Field field : allFields) {
                    if (field.isAnnotationPresent(ExcelProperty.class)) {
                        String[] keywords = field.getAnnotation(ExcelProperty.class).keyword();
                        for (String kw : keywords) {
                            if (headerName.contains(kw.toLowerCase())) {
                                field.setAccessible(true);
                                columnIndexToFieldMap.put(cell.getColumnIndex(), field);
                                break; 
                            }
                        }
                    }
                }
            }

            // ==========================================
            // BƯỚC 2: QUÉT DỮ LIỆU TỪNG DÒNG
            // ==========================================
            for (int rowIndex = headerRowIndex + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    // Nếu gặp dòng trống (chưa có định dạng gì), Excel có thể trả về null, ta coi như hết file
                    break; 
                }

                // KIỂM TRA ĐIỀU KIỆN DỪNG SỚM (PRIMARY COLUMN RỖNG)
                if (primaryColumnIndex != -1) {
                    Cell primaryCell = row.getCell(primaryColumnIndex);
                    String primaryValue = getCellValueAsString(primaryCell).trim();
                    
                    // NẾU CỘT PRIMARY BỊ TRỐNG -> NGẮT VÒNG LẶP ĐỌC FILE NGAY LẬP TỨC
                    if (primaryValue.isEmpty()) {
                        System.out.println("Dừng đọc file tại dòng " + (rowIndex + 1) + " do cột '" + primaryColumnName + "' bị trống.");
                        break;
                    }
                }

                T instance = clazz.getDeclaredConstructor().newInstance();
                boolean isRowEmpty = true;

                if (instance instanceof CommonExcelDto) {
                    ((CommonExcelDto) instance).setExcelRowNumber(row.getRowNum() + 1);
                }

                for (Map.Entry<Integer, Field> entry : columnIndexToFieldMap.entrySet()) {
                    int colIndex = entry.getKey();
                    Field field = entry.getValue();

                    Cell cell = row.getCell(colIndex);
                    Object cellValue = getCellValueAsObject(cell);

                    if (cellValue != null && !cellValue.toString().trim().isEmpty()) {
                        isRowEmpty = false; 
                        Object castedValue = castValue(field.getType(), cellValue);
                        field.set(instance, castedValue);
                    }
                }

                // Nếu user không thiết lập primaryColumn, nhưng dòng đó hoàn toàn trống trơn thì cũng nên dừng
                if (isRowEmpty) {
                    break;
                }
                
                resultList.add(instance);
            }
        }
        return resultList;
    }

    // Các hàm đệ quy và tiện ích (giữ nguyên không đổi)
    private List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        while (type != null && type != Object.class) {
            fields.addAll(Arrays.asList(type.getDeclaredFields()));
            type = type.getSuperclass();
        }
        return fields;
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

