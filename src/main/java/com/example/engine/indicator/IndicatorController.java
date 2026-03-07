package com.example.engine.indicator;

import com.example.engine.common.ApiResp;
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
@RequestMapping("/api/v1/indicator")
@RequiredArgsConstructor
public class IndicatorController {

  private final IndicatorService service;

  @GetMapping
  public ApiResp<List<Indicator>> list() {
    return ApiResp.ok(service.list());
  }

  @PostMapping
  public ApiResp<Indicator> create(@RequestBody Indicator i) {
    return ApiResp.ok(service.create(i));
  }

  @PutMapping("/{id}")
  public ApiResp<Indicator> update(@PathVariable("id") Long id, @RequestBody Indicator patch) {
    return ApiResp.ok(service.update(id, patch));
  }

  @PostMapping("/{id}/disable")
  public ApiResp<Void> disable(@PathVariable("id") Long id) {
    service.disable(id);
    return ApiResp.ok(null);
  }
}
