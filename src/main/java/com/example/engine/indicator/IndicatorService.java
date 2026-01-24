package com.example.engine.indicator;

import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IndicatorService {
  private final IndicatorRepo repo;

  public List<Indicator> list() {
    return repo.findAll();
  }

  @Transactional
  public Indicator create(Indicator i) {
    if (repo.existsByCode(i.getCode())) {
      throw new IllegalArgumentException("指标编码已存在: " + i.getCode());
    }
    return repo.save(i);
  }

  @Transactional
  public Indicator update(Long id, Indicator patch) {
    Indicator db = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("不存在"));
    db.setName(patch.getName());
    db.setMetricType(patch.getMetricType());
    db.setUnit(patch.getUnit());
    db.setEnabled(patch.getEnabled());
    db.setRemark(patch.getRemark());
    return repo.save(db);
  }

  @Transactional
  public void disable(Long id) {
    Indicator db = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("不存在"));
    db.setEnabled(0);
    repo.save(db);
  }
}
