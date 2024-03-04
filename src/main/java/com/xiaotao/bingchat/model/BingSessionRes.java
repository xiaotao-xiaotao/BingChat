package com.xiaotao.bingchat.model;

import lombok.Builder;
import lombok.Data;

/**
 * @author：wangtao
 * @date：2024/3/2 14:27
 */
@Data
@Builder
public class BingSessionRes {

  private long timestamp;
  private boolean startOfSession;
  private int invocationId;
}
