package com.prastavna.leetcode.config;

import com.prastavna.leetcode.utils.Graphql;


public class Leetcode {
  public static final String API_URL = System.getProperty("leetcode.api.url", "https://leetcode.com/graphql");
  public static final int PAGE_SIZE = Integer.parseInt(System.getProperty("leetcode.page.size", "50"));
  public static final int TIMEOUT_MS = Integer.parseInt(System.getProperty("leetcode.timeout.ms", "60000"));
}
