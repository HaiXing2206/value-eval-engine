package com.example.engine.weight;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeightRepo extends JpaRepository<WeightVersion, Long> {
  Optional<WeightVersion> findByVersionCode(String versionCode);
}
