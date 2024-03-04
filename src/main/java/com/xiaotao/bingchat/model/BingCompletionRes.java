/**
 * Copyright 2023 json.cn
 */
package com.xiaotao.bingchat.model;

import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 * @author：xiaotao
 * @date：2023/12/28 17:03
 */
@Data
public class BingCompletionRes {

  private int type;
  private String target;
  private List<Arguments> arguments;
  private List<Arguments> item;

  @Data
  public static class Body {

    private String type;
    private String text;
    private String id;
    private String size;
    private boolean wrap;
  }

  @Data
  public static class AdaptiveCards {

    private String type;
    private String version;
    private List<Body> body;
  }


  @Data
  public static class Feedback {

    private String tag;
    private String updatedOn;
    private String type;
  }

  @Data
  public static class Messages {

    private String text;
    private String author;
    private String hiddenText;
    private String messageType;
    private Date createdAt;
    private Date timestamp;
    private String messageId;
    private String offense;
    private List<AdaptiveCards> adaptiveCards;
    private List<String> sourceAttributions;
    private Feedback feedback;
    private String contentOrigin;
  }

  @Data
  public class Arguments {

    private List<Messages> messages;
    private String requestId;
  }
}
