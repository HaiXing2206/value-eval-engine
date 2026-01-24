package com.example.engine.weight;

import com.example.engine.common.ApiResp;
import com.example.engine.weight.dto.WeightVersionRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/weight")
@RequiredArgsConstructor
public class WeightController {

  private final WeightService service;

  @PostMapping("/version")
  public ApiResp<WeightVersion> createVersion(@Valid @RequestBody WeightVersionRequest req) {
    return ApiResp.ok(service.createVersion(req.getVersionCode(), req.getRemark()));
  }

  @GetMapping("/version/{code}")
  public ApiResp<WeightVersion> getVersion(@PathVariable String code) {
    return ApiResp.ok(service.getByCode(code));
  }

  @PostMapping("/version/{code}/publish")
  public ApiResp<Void> publish(@PathVariable String code) {
    service.publish(code);
    return ApiResp.ok(null);
  }

  @GetMapping("/version/{versionId}/items")
  public ApiResp<List<WeightItem>> listItems(@PathVariable Long versionId) {
    return ApiResp.ok(service.listItems(versionId));
  }

  @PutMapping("/version/{versionId}/items")
  public ApiResp<Void> upsertItems(
      @PathVariable Long versionId, @RequestBody List<WeightItem> items) {
    service.upsertItems(versionId, items);
    return ApiResp.ok(null);
  }
}
