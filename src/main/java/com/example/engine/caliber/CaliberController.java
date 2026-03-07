package com.example.engine.caliber;

import com.example.engine.common.ApiResp;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/caliber")
@RequiredArgsConstructor
public class CaliberController {

  private final CaliberService service;

  @PostMapping
  public ApiResp<CaliberVersion> create(
      @RequestParam(name = "caliberCode") String caliberCode,
      @RequestParam(name = "weightVersionCode") String weightVersionCode,
      @RequestParam(name = "remark", required = false) String remark) {
    return ApiResp.ok(service.create(caliberCode, weightVersionCode, remark));
  }

  @PostMapping("/{caliberCode}/publish")
  public ApiResp<Void> publish(@PathVariable("caliberCode") String caliberCode) {
    service.publish(caliberCode);
    return ApiResp.ok(null);
  }

  @PostMapping("/{caliberCode}/copy")
  public ApiResp<CaliberVersion> copy(
      @PathVariable("caliberCode") String caliberCode,
      @RequestParam(name = "newCaliberCode") String newCaliberCode,
      @RequestParam(name = "remark", required = false) String remark) {
    return ApiResp.ok(service.copyAsDraft(caliberCode, newCaliberCode, remark));
  }

  @PatchMapping("/{caliberCode}")
  public ApiResp<CaliberVersion> updateDraft(
      @PathVariable("caliberCode") String caliberCode, @RequestBody CaliberUpdateReq req) {
    return ApiResp.ok(service.updateDraft(caliberCode, req));
  }

  @GetMapping("/{caliberCode}")
  public ApiResp<CaliberVersion> get(@PathVariable("caliberCode") String caliberCode) {
    return ApiResp.ok(service.get(caliberCode));
  }

  @lombok.Data
  public static class CaliberUpdateReq {
    public String weightVersionCode;
    public String ruleRemark;
    public String missingPolicy; // ZERO/WORST/FAIL
    public BigDecimal levelA;
    public BigDecimal levelB;
    public BigDecimal levelC;
  }
}
