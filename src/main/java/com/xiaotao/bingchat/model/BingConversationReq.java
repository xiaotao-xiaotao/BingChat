/**
 * Copyright 2023 json.cn
 */
package com.xiaotao.bingchat.model;

import cn.hutool.core.collection.CollUtil;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * @author：xiaotao
 * @date：2023/12/28 17:03
 */
@Data
@Builder
public class BingConversationReq {

  private List<Arguments> arguments;
  private String invocationId;
  private String target;
  private int type;

  @Data
  @Builder
  public static class Message {

    @Builder.Default
    private String locale = "zh-CN";
    @Builder.Default
    private String market = "zh-CN";
    @Builder.Default
    private String region = "US";
    @Builder.Default
    private String location = "lat:47.639557;long:-122.128159;re=1000m;";
    private String timestamp;
    private String text;
    private String messageType;
    private String requestId;
    private String messageId;
  }

  @Data
  @Builder
  public static class Participant {

    private String id;
  }

  @Data
  @Builder
  public static class Arguments {

    private String conversationId;
    private String source;
    private boolean isStartOfSession;
    private Message message;
    private Participant participant;
    private String traceId;
    private String requestId;
    private String optionsSets;
    @Builder.Default
    private String verbosity = "verbose";
    @Builder.Default
    private String scenario = "SERP";
    @Builder.Default
    private List<String> plugins = CollUtil.newLinkedList();
    @Builder.Default
    private List<String> conversationHistoryOptionsSets = CollUtil.newLinkedList("autosave",
        "savemem", "uprofupd", "uprofgen");
    @Builder.Default
    private String tone = "Balanced";
    private String spokenTextMode = "None";
  }

}
