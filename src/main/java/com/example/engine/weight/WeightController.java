package com.example.engine.weight;

import com.example.engine.common.ApiResp;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/weight")
@RequiredArgsConstructor
public class WeightController {

  private final WeightRepo verRepo;
  private final WeightService service;

  @GetMapping("/version")
  public ApiResp<List<WeightVersion>> listVersions() {
    return ApiResp.ok(verRepo.findAll());
  }

  @PostMapping("/version")
  public ApiResp<WeightVersion> createVersion(
      @RequestParam String versionCode, @RequestParam(required = false) String remark) {
    return ApiResp.ok(service.createVersion(versionCode, remark));
  }

  @GetMapping("/version/{versionCode}")
  public ApiResp<WeightVersion> getVersion(@PathVariable String versionCode) {
    return ApiResp.ok(service.getByCode(versionCode));
  }

  @GetMapping("/version/{versionCode}/items")
  public ApiResp<List<WeightItem>> listItems(@PathVariable String versionCode) {
    WeightVersion v = service.getByCode(versionCode);
    return ApiResp.ok(service.listItems(v.getId()));
  }

  @PostMapping("/version/{versionCode}/items")
  public ApiResp<Void> upsertItems(
      @PathVariable String versionCode, @RequestBody List<WeightItemReq> items) {
    WeightVersion v = service.getByCode(versionCode);
    List<WeightItem> its =
        items.stream()
            .map(
                req -> {
                  WeightItem it = new WeightItem();
                  it.setVersionId(v.getId());
                  it.setIndicatorCode(req.indicatorCode);
                  it.setWeight(req.weight);
                  return it;
                })
            .toList();
    service.upsertItems(v.getId(), its);
    return ApiResp.ok(null);
  }

  @PostMapping("/version/{versionCode}/publish")
  public ApiResp<Void> publish(@PathVariable String versionCode) {
    service.publish(versionCode);
    return ApiResp.ok(null);
  }

  @Data
  public static class WeightItemReq {
    public String indicatorCode;
    public BigDecimal weight; // 要求和=1
  }
}
