package com.example.engine.caliber;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CaliberRepo extends JpaRepository<CaliberVersion, Long> {
  Optional<CaliberVersion> findByCaliberCode(String code);
}
