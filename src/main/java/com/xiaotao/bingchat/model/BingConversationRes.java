package com.xiaotao.bingchat.model;

import java.net.HttpCookie;
import java.util.List;
import lombok.Data;

/**
 * @author：xiaotao
 * @date：2023/12/28 17:03
 */
@Data
public class BingConversationRes {

  private String conversationId;
  private String clientId;
  //    private Result result;
  private String conversationSignature;
  private List<HttpCookie> cookies;
  private String message;

//    @Data
//    public static class Result {
//      private String value;
//      private String message;
//    }
}
