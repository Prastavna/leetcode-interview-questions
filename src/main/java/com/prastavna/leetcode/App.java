package com.prastavna.leetcode;

import com.prastavna.leetcode.services.Leetcode;

public class App {
  public static void main(String[] args) {
    System.out.println("leetcode");
    
    Leetcode leetcodeClient = new Leetcode();
    try {
      leetcodeClient.fetchPostItems();
    } catch (Exception e) {
      System.err.println("XXX");
    }
  }
}
