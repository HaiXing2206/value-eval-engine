package com.example.engine.caliber;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CaliberService {

  private final CaliberRepo repo;

  public CaliberVersion get(String code) {
    return repo
        .findByCaliberCode(code)
        .orElseThrow(() -> new IllegalArgumentException("口径版本不存在: " + code));
  }

  public CaliberVersion create(String caliberCode, String weightVersionCode, String remark) {
    CaliberVersion v = new CaliberVersion();
    v.setCaliberCode(caliberCode);
    v.setWeightVersionCode(weightVersionCode);
    v.setRuleRemark(remark);
    v.setLevelA(new BigDecimal("90.0"));
    v.setLevelB(new BigDecimal("75.0"));
    v.setLevelC(new BigDecimal("60.0"));
    return repo.save(v);
  }

  @Transactional
  public void publish(String caliberCode) {
    CaliberVersion v = get(caliberCode);
    v.setStatus("PUBLISHED");
    repo.save(v);
  }
}
