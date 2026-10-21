package vn.com.group.insurance.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Dùng để đánh dấu Field này chứa 1 Danh sách (List), đẻ ra một Nhánh mới
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExcelCollection {
    String[] keyword();
    int level();        // Cấp độ của Collection này (vd: Product=2, Content=3)
    Class<?> itemClass(); // Object sẽ được đẻ ra khi match (vd: ProductDto.class)
}
