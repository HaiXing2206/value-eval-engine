package com.example.engine.task;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepo extends JpaRepository<EvalTask, Long> {}
