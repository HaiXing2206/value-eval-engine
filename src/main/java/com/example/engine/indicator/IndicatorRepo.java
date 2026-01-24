package com.example.engine.indicator;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndicatorRepo extends JpaRepository<Indicator, Long> {
  Optional<Indicator> findByCode(String code);

  boolean existsByCode(String code);
}
