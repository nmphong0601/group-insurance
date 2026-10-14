package vn.com.group.insurance.api.mapper;

public interface ExcelTreeMapper<R> {

    // 1. Tạo Node gốc (Level 0 - Tương đương Gói/Package)
    R createRootNode(String name);

    // 2. Tạo Node Cha (Khi dòng đó chứa các dòng con bên trong)
    // level: Cấp độ sâu (1, 2, 3... N)
    Object createParentNode(int level, String name, Object value);

    // 3. Xử lý Node Lá (Khi dòng đó KHÔNG có dòng con)
    // Bạn có thể ép kiểu parentNode, sau đó tùy ý setField hoặc add một Object mới vào List
    void processLeaf(int level, Object parentNode, String name, Object value);

    // 4. Liên kết Node vào Node cha của nó
    void linkNode(int level, Object childNode, Object parentNode);
}