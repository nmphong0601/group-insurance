package vn.com.group.insurance.api.service;

import org.springframework.web.multipart.MultipartFile;
import vn.com.group.insurance.api.dto.PackageDto;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Service interface for Insurance Proposal operations across SQL Server and IBM DB2 datasources.
 */
public interface ExcelService {
    List<Map<String, String>> readDynamicExcel(MultipartFile file) throws Exception;
    List<Map<String, String>> readNormalExcelWithHeader(MultipartFile file) throws IOException;
    List<PackageDto> parseExcel(MultipartFile file) throws Exception;
}
