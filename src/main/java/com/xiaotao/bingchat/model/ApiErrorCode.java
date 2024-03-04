package com.xiaotao.bingchat.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author：xiaotao
 * @Description: 公共返回code
 * @Date: 2023/9/13 16:14
 */
@Getter
@AllArgsConstructor
public enum ApiErrorCode {
  /**
   * 失败
   */
  ERROR(1, "操作失败！"),
  /**
   * 成功
   */
  SUCCESS(0, "操作成功！"),
  ;

  private final Integer code;
  private final String msg;
}
