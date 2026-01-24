package com.example.engine.common;

import lombok.Data;

@Data
public class ApiResp<T> {
  private boolean ok;
  private String msg;
  private T data;

  public static <T> ApiResp<T> ok(T data) {
    ApiResp<T> r = new ApiResp<>();
    r.ok = true;
    r.msg = "OK";
    r.data = data;
    return r;
  }

  public static <T> ApiResp<T> fail(String msg) {
    ApiResp<T> r = new ApiResp<>();
    r.ok = false;
    r.msg = msg;
    return r;
  }
}
