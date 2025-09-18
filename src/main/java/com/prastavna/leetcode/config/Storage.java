package com.prastavna.leetcode.config;

import io.github.cdimascio.dotenv.Dotenv;

public final class Storage {
  private static final Dotenv DOTENV = Dotenv.configure().ignoreIfMissing().load();

  private Storage() {}

  private static String env(String key, String defaultVal) {
    String val = System.getenv(key);
    if (val == null || val.isEmpty()) {
      val = DOTENV.get(key);
    }
    return (val == null || val.isEmpty()) ? defaultVal : val;
  }

  public static final String INTERVIEWS_JSON_PATH = env("INTERVIEWS_JSON_PATH", "interviews.json");
}

