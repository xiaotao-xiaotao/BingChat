package com.xiaotao.bingchat.controller;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import com.xiaotao.bingchat.model.ChatCompletionReq;
import com.xiaotao.bingchat.service.ChatService;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author：xiaotao
 * @date：2023/12/28 17:02
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/chat")
public class ChatController {

  private final ChatService chatService;

  @PostMapping("/completions")
  public void completions(@Valid @RequestBody ChatCompletionReq completionReq,
      HttpServletResponse response)
      throws IOException {
    response.setHeader("Transfer-Encoding", "chunked");
    response.setHeader("Connection", "keep-alive");
    response.setCharacterEncoding(CharsetUtil.UTF_8);
    response.addHeader(Header.CONTENT_TYPE.getValue(), ContentType.TEXT_HTML.getValue());
    try {
      chatService.completions(response, completionReq);
    } catch (Exception e) {
      log.error("internal error：{}", e.getMessage(), e);
    } finally {
      response.getWriter().close();
    }
  }
}
