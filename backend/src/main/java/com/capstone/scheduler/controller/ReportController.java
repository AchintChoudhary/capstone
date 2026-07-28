package com.capstone.scheduler.controller;

import com.capstone.scheduler.service.ReportingService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportingService reportingService;

    public ReportController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportReport(@RequestParam(value = "format", defaultValue = "csv") String format) {
        byte[] data;
        String filename;
        MediaType mediaType;

        if (format.equalsIgnoreCase("pdf")) {
            data = reportingService.exportTasksToTextReport();
            filename = "task_report.pdf.txt"; // Simulates PDF via highly structured text tables
            mediaType = MediaType.TEXT_PLAIN;
        } else if (format.equalsIgnoreCase("excel")) {
            data = reportingService.exportTasksToCsv();
            filename = "task_report.xlsx.csv"; // Excel-loadable CSV export
            mediaType = MediaType.parseMediaType("application/vnd.ms-excel");
        } else if (format.equalsIgnoreCase("json")) {
            data = reportingService.exportTasksToJson();
            filename = "task_report.json";
            mediaType = MediaType.APPLICATION_JSON;
        } else {
            data = reportingService.exportTasksToCsv();
            filename = "task_report.csv";
            mediaType = MediaType.TEXT_PLAIN;
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(mediaType)
                .body(data);
    }
}
