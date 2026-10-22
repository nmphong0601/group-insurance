// ... (Các phần imports và định nghĩa class giữ nguyên)

for (int rowIdx = packageHeaderRowIndex + 1; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
    Row row = sheet.getRow(rowIdx);
    if (row == null) continue;

    String rawLabel = getCellValueAsString(row.getCell(0)).trim();
    if (rawLabel.isEmpty() || rawLabel.equalsIgnoreCase("NaN")) continue;

    String shortLabel = getShortName(rawLabel);
    Object cellValue = parseDynamicValue(getCellValueAsString(row.getCell(col)));

    // Xác định độ sâu (Level) dựa trên cấu trúc file (VD: đếm dấu "_" hoặc theo Indent)
    int currentDepth = countDelimiters(rawLabel); 

    if (cellValue != null && !cellValue.toString().trim().isEmpty()) {
        
        // BƯỚC 1: Cố gắng map vào Thuộc tính (Property) tĩnh
        boolean isHandled = mapToProperty(activeNodes, shortLabel, cellValue, dynamicDictionary);
        
        // BƯỚC 2: Nếu không phải thuộc tính tĩnh, thử tạo Node mới (Collection)
        if (!isHandled) {
            isHandled = createNodeToCollection(activeNodes, currentDepth, shortLabel, cellValue, dynamicDictionary);
        }

        // ==========================================
        // BƯỚC 3: FALLBACK DYNAMIC PROPERTIES
        // Nếu không có cấu hình Rule nào khớp, lưu nó thành dữ liệu động
        // ==========================================
        if (!isHandled) {
            // Tìm Node cha đang kích hoạt gần nhất để hứng dữ liệu
            Object targetNode = getNearestActiveParent(activeNodes, currentDepth);
            
            if (targetNode instanceof CommonTreeExcelDto) {
                // Tiền xử lý dữ liệu để chống lỗi số khoa học (1.2E8)
                Object safeValue = preprocessDynamicValue(cellValue);
                ((CommonTreeExcelDto) targetNode).getDynamicProperties().put(shortLabel, safeValue);
            }
        }
    }
}



// Thay đổi tham số truyền vào của hàm mapToProperty để lấy được instance và gọi castValue
private boolean mapToProperty(Map<Integer, Object> activeNodes, String label, Object value, Map<String, FieldRule> dict) throws Exception {
    String lowerLabel = label.toLowerCase();
    
    for (Map.Entry<Integer, Object> entry : activeNodes.entrySet()) {
        Object obj = entry.getValue();
        if (obj == null) continue;

        for (Field field : obj.getClass().getDeclaredFields()) {
            FieldRule rule = dict.get(field.getName());
            if (rule != null && rule.getLevel() == entry.getKey()) {
                for (String kw : rule.getKeywords()) {
                    if (lowerLabel.contains(kw.toLowerCase())) {
                        field.setAccessible(true);
                        
                        // TRUYỀN THÊM obj VÀ TÊN FIELD VÀO HÀM CAST
                        Object castedValue = castValue(field.getType(), value, obj, field.getName());
                        field.set(obj, castedValue);
                        return true;
                    }
                }
            }
        }
    }
    return false;
}

// Cập nhật castValue
private Object castValue(Class<?> targetType, Object value, Object instance, String fieldName) {
    if (value == null) return null;
    String s = preprocessDynamicValue(value).toString();
    
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
        // TỰ ĐỘNG BẮT LỖI GÁN VÀO DTO
        if (instance instanceof CommonTreeExcelDto) {
            ((CommonTreeExcelDto) instance).addFieldError(fieldName, "Sai định dạng số (Nhận được: '" + s + "')");
        }
        return null;
    }

    if (targetType == String.class) return s;
    return value;
}


package com.example.excelapi.service;

import com.example.excelapi.annotation.ExcelProperty;
import com.example.excelapi.dto.CommonExcelDto;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

@Service
public class ExcelListMappingService {

    // Đã xóa tham số primaryColumnName
    public <T> List<T> parseExcelList(File file, Class<T> clazz, int headerRowIndex) throws Exception {
        List<T> resultList = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(headerRowIndex);
            if (headerRow == null) return resultList;

            Map<Integer, Field> columnIndexToFieldMap = new HashMap<>();
            
            // 2 Biến lưu trữ vị trí và Kiểu dữ liệu của cột Primary
            int primaryColumnIndex = -1; 
            Field primaryField = null;   
            
            List<Field> allFields = getAllFields(clazz);
            
            // ==========================================
            // BƯỚC 1: LẬP BẢN ĐỒ CỘT & TÌM PRIMARY COLUMN
            // ==========================================
            for (Cell cell : headerRow) {
                String headerName = getCellValueAsString(cell).trim().toLowerCase();
                if (headerName.isEmpty()) continue;

                for (Field field : allFields) {
                    if (field.isAnnotationPresent(ExcelProperty.class)) {
                        ExcelProperty ann = field.getAnnotation(ExcelProperty.class);
                        for (String kw : ann.value()) {
                            if (headerName.contains(kw.toLowerCase())) {
                                field.setAccessible(true);
                                columnIndexToFieldMap.put(cell.getColumnIndex(), field);
                                
                                // Nếu Field này được cấu hình là Primary -> Ghi nhớ lại
                                if (ann.primary()) {
                                    primaryColumnIndex = cell.getColumnIndex();
                                    primaryField = field;
                                }
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
                
                // Nếu Excel trả về dòng null hoàn toàn -> Dừng
                if (row == null) break; 

                // KIỂM TRA PRIMARY COLUMN: VỪA KIỂM TRA TRỐNG, VỪA KIỂM TRA KIỂU DỮ LIỆU
                if (primaryColumnIndex != -1 && primaryField != null) {
                    Cell primaryCell = row.getCell(primaryColumnIndex);
                    
                    // Nếu ô không tồn tại hoặc rỗng hoàn toàn
                    if (primaryCell == null || primaryCell.getCellType() == CellType.BLANK) {
                        System.out.println("Dừng đọc file tại dòng " + (rowIndex + 1) + " do ô Primary trống.");
                        break;
                    }

                    Object rawPrimaryValue = getCellValueAsObject(primaryCell);
                    
                    // Mượn hàm castValue để "ép kiểu nháp". 
                    // Truyền null vào instance để hàm castValue không tự động ghi log lỗi vào DTO (vì ta chưa tạo DTO)
                    Object castedPrimary = castValue(primaryField.getType(), rawPrimaryValue, null, primaryField.getName());
                    
                    // Nếu ép kiểu thất bại (trả về null) HOẶC là chuỗi rỗng -> Đó là rác, dừng đọc file ngay.
                    if (castedPrimary == null || castedPrimary.toString().trim().isEmpty()) {
                        System.out.println("Dừng đọc file tại dòng " + (rowIndex + 1) + " do dữ liệu Primary sai kiểu hoặc bị rỗng.");
                        break;
                    }
                }

                // ==========================================
                // NẾU PASS QUA BÀI TEST PRIMARY -> TIẾN HÀNH MAP DỮ LIỆU
                // ==========================================
                T instance = clazz.getDeclaredConstructor().newInstance();
                boolean isRowEmpty = true;

                if (instance instanceof CommonExcelDto) {
                    ((CommonExcelDto) instance).setExcelRowNumber(row.getRowNum() + 1);
                }

                for (Map.Entry<Integer, Field> entry : columnIndexToFieldMap.entrySet()) {
                    int colIndex = entry.getKey();
                    Field field = entry.getValue();
                    Cell cell = row.getCell(colIndex);

                    // Chốt chặn Formula (Đã làm ở bước trước)
                    if (cell != null && cell.getCellType() == CellType.FORMULA) {
                        if (instance instanceof CommonExcelDto) {
                            ((CommonExcelDto) instance).addFieldError(field.getName(), "Không được sử dụng công thức (Formula).");
                        }
                        isRowEmpty = false;
                        continue;
                    }

                    Object cellValue = getCellValueAsObject(cell);

                    if (cellValue != null && !cellValue.toString().trim().isEmpty()) {
                        isRowEmpty = false; 
                        Object castedValue = castValue(field.getType(), cellValue, instance, field.getName());
                        field.set(instance, castedValue);
                    }
                }

                if (isRowEmpty) break;
                
                resultList.add(instance);
            }
        }
        return resultList;
    }

    // (Giữ nguyên các hàm phụ trợ: getAllFields, castValue, getCellValueAsObject, getCellValueAsString)
}

