package edu.apu.pssdk;

import java.util.HashMap;
import java.util.Map;
import psft.pt8.joa.JOAException;

public class Env {

  private static Map<String, String> config;
  private static AppServer appServer;

  static {
    config = new HashMap<>();
    config.put("hostname", System.getenv("APP_SERVER_HOSTNAME"));
    config.put("joltport", System.getenv("APP_SERVER_JOLTPORT"));
    config.put("dmncnpwd", System.getenv("APP_SERVER_DOMAIN_CONNECTION_PASSWORD"));
    config.put("username", System.getenv("APP_SERVER_USERNAME"));
    config.put("password", System.getenv("APP_SERVER_PASSWORD"));

    try {
      appServer = new AppServer(config);
    } catch (JOAException e) {
      System.out.println(e.getMessage());
      throw new RuntimeException(e);
    }
  }

  public static AppServer appServer() throws JOAException {
    return appServer;
  }
}
