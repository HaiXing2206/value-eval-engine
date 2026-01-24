package com.example.engine.export;

import com.example.engine.common.ApiResp;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/task")
@RequiredArgsConstructor
public class ExportController {

  private final ExportService exportService;

  /**
   * JSON导出：summary/detail/snapshot
   */
  @GetMapping("/{taskId}/export")
  public ApiResp<Map<String, Object>> exportJson(
      @PathVariable Long taskId,
      @RequestParam(defaultValue = "summary") String type,
      @RequestParam(defaultValue = "json") String format) {
    if (!"json".equalsIgnoreCase(format)) {
      throw new IllegalArgumentException("format=csv 请走 /export-file 接口");
    }
    return ApiResp.ok(exportService.exportAsMap(taskId, type));
  }

  /**
   * CSV导出（以文件形式返回）
   */
  @GetMapping("/{taskId}/export-file")
  public ResponseEntity<byte[]> exportFile(
      @PathVariable Long taskId,
      @RequestParam(defaultValue = "summary") String type,
      @RequestParam(defaultValue = "csv") String format) {

    if (!"csv".equalsIgnoreCase(format)) {
      throw new IllegalArgumentException("仅支持csv");
    }

    String csv = exportService.exportAsCsv(taskId, type);
    byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);

    String filename = "task_" + taskId + "_" + type + ".csv";
    return ResponseEntity.ok()
        .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
        .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
        .body(bytes);
  }
}
