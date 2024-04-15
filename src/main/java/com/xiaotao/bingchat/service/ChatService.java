package com.xiaotao.bingchat.service;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.xiaotao.bingchat.conf.BasicConfig;
import com.xiaotao.bingchat.constant.Constants;
import com.xiaotao.bingchat.model.BingCompletionRes;
import com.xiaotao.bingchat.model.BingCompletionRes.AdaptiveCards;
import com.xiaotao.bingchat.model.BingCompletionRes.Messages;
import com.xiaotao.bingchat.model.BingConversationRes;
import com.xiaotao.bingchat.model.ChatCompletionReq;
import com.xiaotao.bingchat.utils.ServletUtils;
import java.net.HttpCookie;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

/**
 * @author：xiaotao
 * @date：2023/12/28 17:27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

  private String conversationReqJson;

  private final BasicConfig basicConfig;

  @PostConstruct
  private void init() throws Exception {
    conversationReqJson = IoUtil.readUtf8(
        ResourceUtils.getURL("classpath:json/conversation.json").openStream());
    conversationReqJson = conversationReqJson.replaceAll("\r\n", "").replaceAll(" ", "");
  }

  public void completions(HttpServletResponse response, ChatCompletionReq chatCompletionReq)
      throws Exception {
    ChatHub chatHub = new ChatHub(response, basicConfig, conversationReqJson);
    chatHub.completions(chatCompletionReq.getPrompt());
  }


  public static class ChatHub {

    private final HttpServletResponse servletResponse;
    private final BasicConfig basicConfig;
    private final String conversationReqJson;
    private String msg;

    public ChatHub(HttpServletResponse servletResponse, BasicConfig basicConfig,
        String conversationReqJson) {
      this.servletResponse = servletResponse;
      this.basicConfig = basicConfig;
      this.conversationReqJson = conversationReqJson;
    }

    public void completions(String prompt) throws Exception {
      String cookie = String.format(basicConfig.getCookie(), DateUtil.currentSeconds());
      HttpResponse response = getConversationSignature(cookie);
      if (ObjectUtil.isNull(response) || StrUtil.isEmpty(response.body())) {
        ServletUtils.renderString(servletResponse, "Failed to obtain conversation key！");
        return;
      }
      String conversationRes = response.body();
      log.info("Bing getConversationSignature res：{}", conversationRes);
      if (StrUtil.contains(conversationRes, "Error request, response status: 403")) {
        ServletUtils.renderString(servletResponse, "No permission to access！");
        return;
      }
      if (StrUtil.isEmpty(conversationRes)) {
        ServletUtils.renderString(servletResponse, "Failed to obtain conversation key！");
        return;
      }
      String conversationSignature = response.header("X-Sydney-Encryptedconversationsignature");
      if (StrUtil.isEmpty(conversationSignature)) {
        ServletUtils.renderString(servletResponse, "Failed to obtain conversation key！");
        return;
      }
      BingConversationRes bingConversationRes;
      try {
        bingConversationRes = JSONUtil.toBean(response.body(), BingConversationRes.class);
      } catch (Exception e) {
        log.error("Bing parse conversationRes error：{}", e.getMessage(), e);
        return;
      }
      String bingConversationReq = buildConversationReq(
          bingConversationRes.getConversationId(), bingConversationRes.getClientId(), prompt);
      if (StrUtil.isEmpty(bingConversationReq)) {
        ServletUtils.renderString(servletResponse, "Build conversation request failed！");
        return;
      }

      Request request = buildRequest(
          String.format(Constants.CONVERSATION_CHAT_URL, URLUtil.encodeAll(conversationSignature)),
          response.getCookies(), cookie);
      OkHttpClient mClient = buildOkHttpClient();
      CountDownLatch latch = ThreadUtil.newCountDownLatch(1);
      mClient.newWebSocket(request, new WebSocketListener() {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
          log.info("Successfully connected to websocket！");
          webSocket.send("{\"protocol\":\"json\",\"version\":1}" + "\u001E");
          webSocket.send("{\"type\":6}" + "\u001E");
          webSocket.send(bingConversationReq + "\u001E");
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
          BingCompletionRes bingCompletionRes = JSONUtil.toBean(text, BingCompletionRes.class);
          // Normal reply type=1
          if (bingCompletionRes.getType() == 1) {
            List<Messages> messages = bingCompletionRes.getArguments().get(0).getMessages();
            if (Objects.nonNull(messages) && !messages.isEmpty()) {
              if (StrUtil.equals(messages.get(0).getOffense(), "Unknown")) {
                log.info("Received message：{}", messages.get(0).getText());
                // Handling superscript issues
                String sendMsg;
                String tempMsg = StrUtil.replace(messages.get(0).getText(), "[^", "^")
                    .replace("^]", "^");

                if (StrUtil.isEmpty(msg)) {
                  sendMsg = tempMsg;
                } else {
                  sendMsg = StrUtil.removePrefix(tempMsg, msg);
                }
                ServletUtils.renderString(servletResponse, sendMsg);
                // Assign the latest value for this assignment
                msg = tempMsg;
              }
            }
          } else if (bingCompletionRes.getType() == 2) {
            try {
              List<Messages> messages = bingCompletionRes.getItem().get(0).getMessages();
              if (Objects.nonNull(messages) && messages.size() > 0 && StrUtil.isNotEmpty(msg)) {
                AdaptiveCards adaptiveCard = messages.get(messages.size() - 1).getAdaptiveCards()
                    .get(0);
                int bodySize = adaptiveCard.getBody().size();
                String bodyId = adaptiveCard.getBody().get(bodySize - 1).getId();
                if (StrUtil.isNotEmpty(bodyId)) {
                  String bodyText = adaptiveCard.getBody().get(bodySize - 1).getText();
                  // Add a dividing line
                  ServletUtils.renderString(servletResponse, "\n***\n");
                  List<String> bodyTexts = StrUtil.split(bodyText, "[");
                  for (String s : bodyTexts) {
                    if (StrUtil.contains(s, "http")) {
                      ServletUtils.renderString(servletResponse, "<small>[" + s + "</small>");
                    } else {
                      ServletUtils.renderString(servletResponse, "<small>" + s + "</small>");
                    }
                  }
                }
              }
            } catch (Exception e) {
              log.error("Failed to obtain more information：{}", e.getMessage(), e);
            } finally {
              log.info("Send done");
              webSocket.send("{\"type\":7}" + "\u001E");
              webSocket.close(1000, "Goodbye, Bing!");
            }
          }
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
          log.info("Close websocket connection！");
          if (StrUtil.isEmpty(msg)) {
            ServletUtils.renderString(servletResponse, "Failed to obtain conversation！");
          }
          latch.countDown();
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable throwable, Response response) {
          log.info("Connection to websocket failed！");
          ServletUtils.renderString(servletResponse, "Connection Bing failed！");
          latch.countDown();
        }
      });
      latch.await();
      log.info("------------------end-------------------");
    }

    private OkHttpClient buildOkHttpClient() {
      Proxy proxy = Proxy.NO_PROXY;
      if (basicConfig.isProxyEnabled()) {
        proxy = new Proxy(Proxy.Type.HTTP,
            new InetSocketAddress(basicConfig.getProxyIp(), basicConfig.getProxyPort()));
      }
      return new OkHttpClient.Builder()
          .proxy(proxy)
          .readTimeout(3, TimeUnit.SECONDS)
          .writeTimeout(3, TimeUnit.SECONDS)
          .connectTimeout(3, TimeUnit.SECONDS)
          .build();
    }

    private Request buildRequest(String url, List<HttpCookie> cookies, String cookie) {
      return new Request.Builder()
          .get()
          .url(url)
          .addHeader(Header.ACCEPT_ENCODING.getValue(), "gzip, deflate, br, zstd")
          .addHeader(Header.ACCEPT_LANGUAGE.getValue(), "en")
          .addHeader(Header.CACHE_CONTROL.getValue(), "no-cache")
          .addHeader(Header.PRAGMA.getValue(), "no-cache")
          .addHeader(Header.UPGRADE.getValue(), "websocket")
          .addHeader(Header.HOST.getValue(), "sydney.bing.com")
          .addHeader(Header.REFERER.getValue(), "https://www.bing.com/search")
          .addHeader(Header.ORIGIN.getValue(), "https://www.bing.com")
          .addHeader(Header.CONNECTION.getValue(), "Upgrade")
          .addHeader(Header.USER_AGENT.getValue(), basicConfig.getUserAgent())
          .addHeader(Header.COOKIE.getValue(), StrUtil.join(";", cookies) + ";" + cookie)
          .build();
    }

    private HttpResponse getConversationSignature(String cookie) {
      HttpResponse response = null;
      try {
        Proxy proxy = Proxy.NO_PROXY;
        if (basicConfig.isProxyEnabled()) {
          proxy = new Proxy(Proxy.Type.HTTP,
              new InetSocketAddress(basicConfig.getProxyIp(), basicConfig.getProxyPort()));
        }
        Map<String, String> param = MapUtil.newHashMap();
        param.put(Header.REFERER.getValue(),
            "https://www.bing.com/chat?q=Bing+AI&FORM=hpcodx&showconv=1&toWww=1&redig="
                + IdUtil.fastSimpleUUID().toUpperCase());
        param.put(Header.USER_AGENT.getValue(), basicConfig.getUserAgent());
        param.put("X-Ms-Client-Request-Id", IdUtil.fastUUID());
        param.put("X-Ms-Useragent",
            "azsdk-js-api-client-factory/1.0.0-beta.1 core-rest-pipeline/1.15.1 OS/Windows");
        response = HttpRequest.get(
                String.format(Constants.CONVERSATION_SIGNATURE_URL, basicConfig.getBundleVersion()))
            .setProxy(proxy)
            .timeout(60000)
            .cookie(cookie)
            .headerMap(param, true)
            .execute();
      } catch (Exception e) {
        log.error("GetConversationSignature error：{}", e.getMessage(), e);
      }
      return response;
    }

    private String buildConversationReq(String conversationId,
        String clientId, String prompt) {
      return conversationReqJson.replace("$plugin_id", IdUtil.fastUUID())
          .replace("$trace_id", IdUtil.fastSimpleUUID())
          .replace("$request_id", IdUtil.fastUUID())
          .replace("$timestamp",
              DateUtil.format(DateUtil.date(), DatePattern.UTC_WITH_XXX_OFFSET_PATTERN))
          .replace("$text", prompt)
          .replace("$conversation_id", conversationId)
          .replace("$client_id", clientId)
          .replace("$is_start_of_session", "true")
          .replace("$invocation_id", "0");
    }
  }
}
