package vn.com.group.insurance.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class ConfigRule {
    private String targetField; // Tên biến trong Java
    private List<String> matchKeywords; // Danh sách từ khóa

    public ConfigRule(String targetField, List<String> matchKeywords) {
        this.targetField = targetField;
        this.matchKeywords = matchKeywords;
    }
}
