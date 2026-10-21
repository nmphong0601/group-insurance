package vn.com.group.insurance.api.entity.sqlserver;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ExcelRuleEntity {
    private String targetField;
    private int targetLevel;
    private List<String> matchKeywords;
}
