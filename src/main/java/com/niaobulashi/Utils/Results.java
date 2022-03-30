package com.niaobulashi.Utils;

import lombok.Builder;

import java.text.MessageFormat;
import java.util.Objects;

/**
 * result
 *
 * @param <T> T
 */
@Builder
public class Results<T> {
  /** status */
  private Integer code;

  /** message */
  private String message;

  public Results(Integer code, String message, T data) {
    this.code = code;
    this.message = message;
    this.data = data;
  }

  /** data */
  private T data;

  public Results() {}

  public Results(Integer code, String message) {
    this.code = code;
    this.message = message;
  }

  public Results(T data) {
    this.code = 0;
    this.data = data;
  }

  private Results(Status status) {
    if (status != null) {
      this.code = status.getCode();
      this.message = status.getMsg();
    }
  }

  @Override
  public String toString() {
    return "Result{" +
            "code=" + code +
            ", message='" + message + '\'' +
            ", data=" + data +
            '}';
  }

  /**
   * Call this function if there is success
   *
   * @param data data
   * @param <T> type
   * @return resule
   */
  public static <T> Result<T> success(T data) {
    return new Result<>(data);
  }

  /**
   * Call this function if there is any error
   *
   * @param status status
   * @return result
   */
  public static Result error(Status status) {
    return new Result(status);
  }

  /**
   * Call this function if there is any error
   *
   * @param status status
   * @param args args
   * @return result
   */
  public static Result errorWithArgs(Status status, Object... args) {
    return new Result(status.getCode(), MessageFormat.format(status.getMsg(), args));
  }

  public Integer getCode() {
    return code;
  }

  public void setCode(Integer code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Results<?> result = (Results<?>) o;
    return Objects.equals(code, result.code) && Objects.equals(message, result.message) && Objects.equals(data, result.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, message, data);
  }

}
