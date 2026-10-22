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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import org.apache.poi.ss.usermodel.DateUtil;

private Object castValue(Class<?> targetType, Object value) {
    if (value == null) return null;

    // ==========================================
    // MỚI: XỬ LÝ NHÓM NGÀY THÁNG NĂM (DATE / TIME)
    // ==========================================
    if (targetType == Date.class || targetType == LocalDate.class || targetType == LocalDateTime.class) {
        Date parsedDate = null;

        // Trường hợp 1: File Excel lưu đúng định dạng Date (đã được getCellValueAsObject lấy ra)
        if (value instanceof Date) {
            parsedDate = (Date) value;
        } 
        // Trường hợp 2: Excel lưu ngày tháng dưới dạng Số (Serial Date)
        else if (value instanceof Number) {
            parsedDate = DateUtil.getJavaDate(((Number) value).doubleValue());
        } 
        // Trường hợp 3: Người dùng nhập dạng Text (Chuỗi)
        else {
            String dateStr = value.toString().trim();
            // Danh sách các format thông dụng tại VN và Quốc tế
            String[] formats = {
                "dd/MM/yyyy", "dd-MM-yyyy", "yyyy-MM-dd", "yyyy/MM/dd", "MM/dd/yyyy", 
                "dd/MM/yyyy HH:mm:ss", "yyyy-MM-dd HH:mm:ss"
            };
            
            for (String format : formats) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                    if (format.contains("HH")) {
                        LocalDateTime ldt = LocalDateTime.parse(dateStr, formatter);
                        parsedDate = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
                    } else {
                        LocalDate ld = LocalDate.parse(dateStr, formatter);
                        parsedDate = Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    }
                    break; // Nếu parse thành công thì dừng vòng lặp
                } catch (DateTimeParseException ignored) {
                    // Bỏ qua lỗi và thử format tiếp theo
                }
            }
        }

        // Ép kiểu Date tìm được ra đúng kiểu dữ liệu khai báo trong DTO
        if (parsedDate != null) {
            if (targetType == Date.class) return parsedDate;
            if (targetType == LocalDate.class) return parsedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (targetType == LocalDateTime.class) return parsedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        
        System.err.println("Cảnh báo: Không thể ép giá trị '" + value + "' sang Ngày tháng.");
        return null;
    }

    // ==========================================
    // XỬ LÝ CÁC KIỂU CƠ BẢN CŨ (String, Number, Boolean)
    // ==========================================
    String s;
    if (value instanceof Number) {
        s = new BigDecimal(value.toString()).stripTrailingZeros().toPlainString();
    } else {
        s = value.toString().trim();
        if (s.toUpperCase().contains("E")) {
            try { s = new BigDecimal(s).stripTrailingZeros().toPlainString(); } 
            catch (Exception ignored) {}
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
        if (targetType == Float.class || targetType == float.class) return new BigDecimal(s).floatValue();
        if (targetType == BigDecimal.class) return new BigDecimal(s);
    } catch (Exception e) {
        System.err.println("Cảnh báo: Không thể ép giá trị '" + s + "' sang " + targetType.getSimpleName());
        return null;
    }

    if (targetType == String.class) return s;

    return value;
}

// Cập nhật tham số đầu vào
private Object castValue(Class<?> targetType, Object value, Object instance, String fieldName) {
    if (value == null) return null;

    // ... (Giữ nguyên logic parse Date/Time đã viết trước đó) ...
    // Khúc try-catch parse Date, nếu sinh lỗi DateTimeParseException:
    /*
        } catch (DateTimeParseException ignored) {
            // Không break, để vòng lặp chạy tiếp
        }
    */
    // Sau khi kết thúc vòng lặp Date mà parsedDate vẫn null, báo lỗi:
    /*
        if (parsedDate == null) {
            logError(instance, fieldName, "Sai định dạng Ngày tháng (Nhận được: '" + value + "')");
            return null;
        }
    */

    String s;
    if (value instanceof Number) {
        s = new BigDecimal(value.toString()).stripTrailingZeros().toPlainString();
    } else {
        s = value.toString().trim();
        if (s.toUpperCase().contains("E")) {
            try { s = new BigDecimal(s).stripTrailingZeros().toPlainString(); } 
            catch (Exception ignored) {}
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
        if (targetType == Float.class || targetType == float.class) return new BigDecimal(s).floatValue();
        if (targetType == BigDecimal.class) return new BigDecimal(s);
    } catch (Exception e) {
        // TỰ ĐỘNG BẮT LỖI GÁN VÀO DTO
        logError(instance, fieldName, "Sai định dạng số (Nhận được: '" + s + "')");
        return null;
    }

    if (targetType == String.class) return s;

    return value;
}

// Hàm phụ trợ ghi log lỗi
private void logError(Object instance, String fieldName, String errorMsg) {
    if (instance instanceof CommonExcelDto) {
        ((CommonExcelDto) instance).addFieldError(fieldName, errorMsg);
    } else {
        System.err.println("Cảnh báo Field [" + fieldName + "]: " + errorMsg);
    }
}

