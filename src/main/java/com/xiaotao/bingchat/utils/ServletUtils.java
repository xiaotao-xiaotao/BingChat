package com.xiaotao.bingchat.utils;

import cn.hutool.core.util.CharsetUtil;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 客户端工具类
 *
 * @author xiaotao
 */
@Slf4j
public class ServletUtils {

  /**
   * 将字符串渲染到客户端
   *
   * @param response 渲染对象
   * @param msg 待渲染的字符串
   */
  public static void renderString(HttpServletResponse response, String msg) {
    try {
      response.setStatus(200);
      response.setCharacterEncoding(CharsetUtil.UTF_8);
      response.getWriter().print(msg);
      response.getWriter().flush();
    } catch (Exception e) {
      log.error("renderString error：{}", e.getMessage(), e);
    }
  }
}
