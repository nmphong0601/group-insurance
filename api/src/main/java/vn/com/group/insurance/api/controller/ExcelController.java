package vn.com.group.insurance.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.com.group.insurance.api.dto.*;
import vn.com.group.insurance.api.service.ExcelService;

import java.util.List;
import java.util.Map;

/**
 * REST Controller exposing API microservice endpoints.
 */
@RestController
@RequestMapping("/api/v1/excel")
public class ExcelController {

    private final ExcelService excelService;

    public ExcelController(ExcelService excelService) {
        this.excelService = excelService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please upload an Excel file.");
        }

        try {
            List<Map<String, String>> result = excelService.readDynamicExcel(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to read Excel file: " + e.getMessage());
        }
    }
}
