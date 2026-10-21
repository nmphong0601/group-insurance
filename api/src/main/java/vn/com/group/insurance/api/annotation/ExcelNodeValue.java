package vn.com.group.insurance.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 4. Hứng Value (TRUE/FALSE) của dòng tạo ra Node cha
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExcelNodeValue {}
