package com.example.engine.task;

import com.example.engine.common.ApiResp;
import com.example.engine.task.dto.CalcRequest;
import com.example.engine.task.dto.CalcResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/task")
@RequiredArgsConstructor
public class TaskController {

  private final TaskRepo taskRepo;
  private final ResultRepo resultRepo;
  private final CalcService calcService;

  @PostMapping("/calculate")
  public ApiResp<CalcResponse> calculate(@Valid @RequestBody CalcRequest req) {
    return ApiResp.ok(calcService.calculate(req));
  }

  @GetMapping("/{taskId}")
  public ApiResp<EvalTask> getTask(@PathVariable Long taskId) {
    return ApiResp.ok(
        taskRepo.findById(taskId).orElseThrow(() -> new IllegalArgumentException("任务不存在")));
  }

  @GetMapping("/{taskId}/result")
  public ApiResp<EvalResult> getResult(@PathVariable Long taskId) {
    return ApiResp.ok(
        resultRepo.findByTaskId(taskId).orElseThrow(() -> new IllegalArgumentException("结果不存在")));
  }
}
