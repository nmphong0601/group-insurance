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

