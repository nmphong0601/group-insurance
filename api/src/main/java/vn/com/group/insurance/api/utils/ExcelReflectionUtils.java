package vn.com.group.insurance.api.utils;

import vn.com.group.insurance.api.annotation.ExcelProperty;

import java.lang.reflect.Field;
import java.math.BigDecimal;

public class ExcelReflectionUtils {
    public static void injectValue(Object target, String excelLabel, Object value) {
        if (target == null || excelLabel == null || value == null) return;

        Class<?> clazz = target.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            ExcelProperty annotation = field.getAnnotation(ExcelProperty.class);

            if (annotation != null) {
                // TỐI ƯU CỰC MẠNH: Map tương đối (Contains)
                // Chỉ cần Label trên Excel CHỨA từ khóa trong Annotation là hợp lệ
                if (excelLabel.toLowerCase().contains(annotation.value().toLowerCase())) {
                    field.setAccessible(true);
                    try {
                        Object parsedValue = castToFieldType(field.getType(), value);
                        field.set(target, parsedValue);
                    } catch (Exception e) {
                        System.err.println("Không thể map field: " + field.getName() + " - Lỗi: " + e.getMessage());
                    }
                    return; // Gán thành công thì dừng duyệt thuộc tính
                }
            }
        }
    }

    // Tự động ép kiểu (Long, Int, Double, Boolean) để không bị lỗi Type Mismatch
    private static Object castToFieldType(Class<?> fieldType, Object value) {
        // 1. Chống lỗi Scientific Notation (1.2E8) ngay từ đầu vào
        String strVal;
        if (value instanceof Double || value instanceof Float) {
            strVal = new BigDecimal(value.toString()).stripTrailingZeros().toPlainString();
        } else {
            strVal = value.toString().trim();
        }

        if (fieldType == Integer.class || fieldType == int.class) {
            return new BigDecimal(strVal).intValue();
        } else if (fieldType == Long.class || fieldType == long.class) {
            return new BigDecimal(strVal).longValue();
        } else if (fieldType == Double.class || fieldType == double.class) {
            return new BigDecimal(strVal).doubleValue();
        } else if (fieldType == Boolean.class || fieldType == boolean.class) {
            return Boolean.parseBoolean(strVal.toUpperCase());
        } else if (fieldType == String.class) {
            // 2. Chống hiện tượng chuỗi bị dư số thập phân (Ví dụ "120.0" -> "120")
            try {
                return new BigDecimal(strVal).stripTrailingZeros().toPlainString();
            } catch (Exception e) {
                return strVal; // Nếu không phải số (ví dụ: "Nhân viên"), trả về chuỗi gốc
            }
        }
        return value;
    }
}
