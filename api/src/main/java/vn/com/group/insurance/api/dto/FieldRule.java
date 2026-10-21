package vn.com.group.insurance.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldRule {
    private Integer index;         // Row/Column Index (Ưu tiên tuyệt đối nếu có giá trị)
    private String code;           // Row/Column Code (Dành cho các file có mã code ẩn hoặc prefix chuẩn)
    private String name;           // Row/Column Name (Dùng hàm Contains để match linh hoạt)

    private int level;             // Cấp độ cấu trúc cây (0, 1, 2...)
    private Class<?> dataType;     // Ép kiểu đích (String.class, Boolean.class, Long.class...)
    private Direction direction;   // Hướng quét: ROW hoặc COLUMN

    public enum Direction {
        ROW,
        COLUMN
    }
}