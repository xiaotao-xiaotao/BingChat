package com.xiaotao.bingchat.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author：xiaotao
 * @date：2024/1/17 10:37
 */
@Data
@Component
@ConfigurationProperties(prefix = "basic")
public class BasicConfig {

  private boolean proxyEnabled;
  private String proxyIp;
  private int proxyPort;
  private String cookie;
  private String userAgent;
  private String bundleVersion;
}
