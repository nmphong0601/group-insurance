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
