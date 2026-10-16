package vn.com.group.insurance.api.mapper;

public interface ExcelTreeMapper<R> {
    R createRootNode(String name);
    
    // Đã thay đổi: Nhận parentNode để Controller tự quyết định sẽ tạo ra Class con gì
    Object createParentNode(Object parentNode, String name, Object value);
    
    // Đã thay đổi: Truyền thẳng vào parent
    void linkNode(Object childNode, Object parentNode);
    
    void processLeaf(Object parentNode, String name, Object value);
}
