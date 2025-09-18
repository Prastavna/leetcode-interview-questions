package com.prastavna.leetcode.config;

import io.github.cdimascio.dotenv.Dotenv;

public final class Leetcode {
  private static final Dotenv DOTENV = Dotenv.configure().ignoreIfMissing().load();

  private Leetcode() {}

  private static String env(String key, String defaultVal) {
    String val = System.getenv(key);
    if (val == null || val.isEmpty()) {
      val = DOTENV.get(key);
    }
    return (val == null || val.isEmpty()) ? defaultVal : val;
  }

  private static int envInt(String key, int defaultVal) {
    String raw = env(key, Integer.toString(defaultVal));
    try {
      return Integer.parseInt(raw.trim());
    } catch (NumberFormatException e) {
      return defaultVal;
    }
  }

  public static final String API_URL = env("LEETCODE_API_URL", "https://leetcode.com/graphql");
  public static final int PAGE_SIZE = envInt("LEETCODE_PAGE_SIZE", 50);
  public static final int TIMEOUT_MS = envInt("LEETCODE_TIMEOUT_MS", 60000);
  public static final int LAG_DAYS = envInt("LEETCODE_LAG_DAYS", 7);
  public static final String FETCH_START_DATE = env("LEETCODE_FETCH_START_DATE", "2024-01-01");
}
