package com.xiaotao.bingchat.model;

import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author：xiaotao
 * @date：2023/12/28 17:03
 */
@Data
public class ChatCompletionReq {

  @NotBlank(message = "输入内容不能为空")
  private String prompt;
}


