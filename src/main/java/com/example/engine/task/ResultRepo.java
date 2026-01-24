package com.example.engine.task;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResultRepo extends JpaRepository<EvalResult, Long> {
  Optional<EvalResult> findByTaskId(Long taskId);
}
