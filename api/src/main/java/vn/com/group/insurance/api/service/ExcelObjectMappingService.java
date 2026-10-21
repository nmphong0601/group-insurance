package vn.com.group.insurance.api.service;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.com.group.insurance.api.annotation.ExcelCollection;
import vn.com.group.insurance.api.annotation.ExcelNodeName;
import vn.com.group.insurance.api.annotation.ExcelNodeValue;
import vn.com.group.insurance.api.annotation.ExcelProperty;
import vn.com.group.insurance.api.dto.FieldRule;
import vn.com.group.insurance.api.dto.PackageDto;
import vn.com.group.insurance.api.dto.PolicyDto;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

@Service
public class ExcelObjectMappingService {

    public PolicyDto parseExcelByDictionary(
            MultipartFile file,
            int packageHeaderRowIndex,
            Map<String, FieldRule> dynamicDictionary // MAP CHỨA CẢ LEVEL & KEYWORDS
    ) throws Exception {
        PolicyDto policy = new PolicyDto();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // 1. Quét phần Header (Policy)
            for (int i = 0; i < packageHeaderRowIndex; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String label = getCellValueAsString(row.getCell(0));
                Object value = parseDynamicValue(getCellValueAsString(row.getCell(1)));
                if (!label.isEmpty()) mapToProperty(Map.of(0, policy), i, 0, label, "", value, dynamicDictionary);
            }

            Row pkgHeaderRow = sheet.getRow(packageHeaderRowIndex);
            if (pkgHeaderRow == null) return policy;

            // 2. Quét các Cột (Package)
            for (int col = 1; col < pkgHeaderRow.getLastCellNum(); col++) {
                String pkgName = getCellValueAsString(pkgHeaderRow.getCell(col)).trim();
                if (pkgName.isEmpty()) continue;

                // Tự build Package
                PackageDto pkg = new PackageDto();
                pkg.setPackageName(pkgName);

                Map<Integer, Object> activeNodes = new LinkedHashMap<>();
                activeNodes.put(0, policy); // Policy luôn là Root cấp 0
                activeNodes.put(1, pkg);    // Package luôn là Root dọc cấp 1

                for (int rowIdx = packageHeaderRowIndex + 1; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                    Row row = sheet.getRow(rowIdx);
                    if (row == null) continue;

                    String rawLabel = getCellValueAsString(row.getCell(0)).trim();
                    if (rawLabel.isEmpty() || rawLabel.equalsIgnoreCase("NaN")) continue;

                    String shortLabel = getShortName(rawLabel);
                    Object value = parseDynamicValue(getCellValueAsString(row.getCell(col)));

                    // LẦN LƯỢT MAP VÀO THUỘC TÍNH HOẶC TẠO NODE MỚI
                    boolean isHandled = mapToProperty(activeNodes, rowIdx, col, shortLabel, "", value, dynamicDictionary);
                    if (!isHandled) {
                        createNodeToCollection(activeNodes, rowIdx, col, shortLabel, "", value, dynamicDictionary);
                    }
                }
                policy.getPackages().add(pkg);
            }
        }
        return policy;
    }

    // ==========================================
    // LOGIC TÌM KIẾM THEO TỪ ĐIỂN VÀ LEVEL
    // ==========================================

    private Object parseDynamicValue(String value) {
        if (value == null || value.isEmpty()) return null;

        String upper = value.toUpperCase();
        if (upper.equals("TRUE") || upper.equals("FALSE")) return Boolean.parseBoolean(upper);

        try {
            BigDecimal bd = new BigDecimal(value);
            if (bd.scale() <= 0) return bd.longValueExact();
            else return bd.doubleValue();
        } catch (Exception e) {
            return value;
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return BigDecimal.valueOf(cell.getNumericCellValue()).toPlainString();
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    private void injectNodeNameAndValue(Object node, String name, Object value) throws Exception {
        for (Field field : node.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(ExcelNodeName.class)) {
                field.setAccessible(true);
                field.set(node, name);
            }
            if (field.isAnnotationPresent(ExcelNodeValue.class)) {
                field.setAccessible(true);
                field.set(node, castValue(field.getType(), value));
            }
        }
    }

    // --- CÁC HÀM REFLECTION KẾT HỢP TỪ ĐIỂN ---

    private String getShortName(String fullLabel) {
        if (fullLabel == null || fullLabel.isEmpty()) return "";
        String[] parts = fullLabel.split("_");
        return parts[parts.length - 1].trim();
    }

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

    private boolean isMatch(FieldRule rule, int currentIndex, String currentLabel, String currentCode) {
        if (rule == null) return false;

        // 1. Định vị bằng Index (Nhanh và chính xác nhất, O(1))
        if (rule.getIndex() != null && rule.getIndex() == currentIndex) {
            return true;
        }

        // 2. Định vị bằng Code (So khớp chính xác - Exact Match)
        if (rule.getCode() != null && !rule.getCode().isEmpty()) {
            if (rule.getCode().equalsIgnoreCase(currentCode)) return true;
        }

        // 3. Định vị bằng Name (So khớp tương đối - Contains)
        if (rule.getName() != null && !rule.getName().isEmpty() && currentLabel != null) {
            if (currentLabel.toLowerCase().contains(rule.getName().toLowerCase())) return true;
        }

        return false;
    }

    private boolean mapToProperty(
            Map<Integer, Object> activeNodes,
            int currentRowIdx,
            int currentColIdx,
            String rowLabel,
            String colLabel, // Bổ sung label của cột
            Object cellValue,
            Map<String, FieldRule> dict
    ) throws Exception {

        for (Map.Entry<Integer, Object> entry : activeNodes.entrySet()) {
            Object obj = entry.getValue();
            if (obj == null) continue;

            for (Field field : obj.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(ExcelProperty.class)) {
                    FieldRule rule = dict.get(field.getName());

                    if (rule != null && rule.getLevel() == entry.getKey()) {
                        boolean isMatched = false;

                        // RẼ NHÁNH THEO HƯỚNG QUÉT (DIRECTION)
                        if (rule.getDirection() == FieldRule.Direction.ROW) {
                            isMatched = isMatch(rule, currentRowIdx, rowLabel, ""); // Giả sử chưa có hệ thống rowCode
                        } else if (rule.getDirection() == FieldRule.Direction.COLUMN) {
                            isMatched = isMatch(rule, currentColIdx, colLabel, "");
                        }

                        if (isMatched) {
                            field.setAccessible(true);
                            // ÉP KIỂU THEO ĐỊNH NGHĨA TRONG RULE (Không phụ thuộc vào field.getType() nữa)
                            Object castedValue = castValue(rule.getDataType(), cellValue);
                            field.set(obj, castedValue);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean createNodeToCollection(Map<Integer, Object> activeNodes, int currentRowIdx, int currentColIdx, String rowLabel, String colLabel, Object cellValue, Map<String, FieldRule> dict) throws Exception {
        String rowLowerLabel = rowLabel.toLowerCase();
        String colLowerLabel = colLabel.toLowerCase();

        for (Map.Entry<Integer, Object> entry : activeNodes.entrySet()) {
            Object parentObj = entry.getValue();
            if (parentObj == null) continue;

            for (Field field : parentObj.getClass().getDeclaredFields()) {
                ExcelCollection ann = field.getAnnotation(ExcelCollection.class);
                if (ann != null) {

                    FieldRule rule = dict.get(field.getName());

                    if (rule != null && rule.getLevel() == entry.getKey()) {
                        boolean isMatched = false;

                        // RẼ NHÁNH THEO HƯỚNG QUÉT (DIRECTION)
                        if (rule.getDirection() == FieldRule.Direction.ROW) {
                            isMatched = isMatch(rule, currentRowIdx, rowLowerLabel, ""); // Giả sử chưa có hệ thống rowCode
                        } else if (rule.getDirection() == FieldRule.Direction.COLUMN) {
                            isMatched = isMatch(rule, currentColIdx, colLowerLabel, "");
                        }

                        if (isMatched) {
                            Object newNode = ann.itemClass().getDeclaredConstructor().newInstance();
                            if (rule.getDirection() == FieldRule.Direction.ROW) {
                                injectNodeNameAndValue(newNode, rowLowerLabel, cellValue);
                            } else if (rule.getDirection() == FieldRule.Direction.COLUMN) {
                                injectNodeNameAndValue(newNode, colLowerLabel, cellValue);
                            }

                            field.setAccessible(true);
                            @SuppressWarnings("unchecked")
                            List<Object> list = (List<Object>) field.get(parentObj);
                            if (list == null) {
                                list = new ArrayList<>();
                                field.set(parentObj, list);
                            }
                            list.add(newNode);

                            activeNodes.put(rule.getLevel(), newNode);
                            activeNodes.keySet().removeIf(k -> k > rule.getLevel());
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

//    private boolean mapToProperty(Map<Integer, Object> activeNodes, String label, Object value, Map<String, FieldRule> dict) throws Exception {
//        String lowerLabel = label.toLowerCase();
//
//        for (Map.Entry<Integer, Object> entry : activeNodes.entrySet()) {
//            Object obj = entry.getValue();
//            if (obj == null) continue;
//
//            for (Field field : obj.getClass().getDeclaredFields()) {
//                if (field.isAnnotationPresent(ExcelProperty.class)) {
//
//                    FieldRule rule = dict.get(field.getName()); // Tra cứu Level & Keywords
//
//                    // Nếu rule tồn tại VÀ level của thuộc tính bằng với level của Node đang chứa nó
//                    if (rule != null && rule.getLevel() == entry.getKey()) {
//                        for (String kw : rule.getKeywords()) {
//                            if (lowerLabel.contains(kw.toLowerCase())) {
//                                field.setAccessible(true);
//                                field.set(obj, castValue(field.getType(), value));
//                                return true;
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return false;
//    }

//    private boolean createNodeToCollection(Map<Integer, Object> activeNodes, String label, Object value, Map<String, FieldRule> dict) throws Exception {
//        String lowerLabel = label.toLowerCase();
//
//        for (Map.Entry<Integer, Object> entry : activeNodes.entrySet()) {
//            Object parentObj = entry.getValue();
//            if (parentObj == null) continue;
//
//            for (Field field : parentObj.getClass().getDeclaredFields()) {
//                ExcelCollection ann = field.getAnnotation(ExcelCollection.class);
//                if (ann != null) {
//
//                    FieldRule rule = dict.get(field.getName());
//
//                    // Logic mấu chốt: Một Collection sinh ra các item ở cấp N, thì Node chứa collection đó phải ở cấp N-1.
//                    // Do đó: rule.getLevel() == entry.getKey() + 1
//                    if (rule != null && rule.getLevel() == entry.getKey() + 1) {
//                        for (String kw : rule.getKeywords()) {
//                            if (lowerLabel.contains(kw.toLowerCase())) {
//
//                                Object newNode = ann.itemClass().getDeclaredConstructor().newInstance();
//                                injectNodeNameAndValue(newNode, label, value);
//
//                                field.setAccessible(true);
//                                @SuppressWarnings("unchecked")
//                                List<Object> list = (List<Object>) field.get(parentObj);
//                                if (list == null) {
//                                    list = new ArrayList<>();
//                                    field.set(parentObj, list);
//                                }
//                                list.add(newNode);
//
//                                activeNodes.put(rule.getLevel(), newNode);
//                                activeNodes.keySet().removeIf(k -> k > rule.getLevel());
//                                return true;
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return false;
//    }
}
