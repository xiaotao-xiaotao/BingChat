package com.xiaotao.bingchat.constant;

import cn.hutool.core.map.MapUtil;
import com.xiaotao.bingchat.model.BingSessionRes;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author：xiaotao
 * @date：2024/2/21 11:31
 */
public class Constants {

  public static final ConcurrentHashMap<String, BingSessionRes> map = MapUtil.newConcurrentHashMap();

  public static final String CONVERSATION_SIGNATURE_URL = "https://www.bing.com/turing/conversation/create?bundleVersion=%s";

  public static final String CONVERSATION_CHAT_URL = "wss://sydney.bing.com/sydney/ChatHub?sec_access_token=%s";
}
