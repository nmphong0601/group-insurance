package vn.com.group.insurance.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Dùng để map các dữ liệu đơn (vd: Tính tiền, Lương)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExcelProperty {
    String[] keyword(); // Mảng từ khóa để bắt. VD: {"Tính tiền", "Lương"}
    int level();        // Khai báo cấp độ cố định (Policy=0, Package=1, Product=2...)
}