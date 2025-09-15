package com.prastavna.leetcode;

import java.util.Arrays;

import com.prastavna.leetcode.models.DiscussPostDetail;
import com.prastavna.leetcode.models.DiscussPostItems;
import com.prastavna.leetcode.services.Leetcode;
import com.prastavna.leetcode.services.Openai;

import io.github.cdimascio.dotenv.Dotenv;

public class App {
  public static void main(String[] args) {
    System.out.println("leetcode");
    Dotenv dotenv = Dotenv.load();

    System.out.println(dotenv.get("OPENAI_BASE_URL"));
   
    Openai openai = new Openai(dotenv.get("OPENAI_BASE_URL"), dotenv.get("OPENAI_API_KEY"));
    openai.getJsonCompletion();
  
    Leetcode leetcodeClient = new Leetcode();
    try {
      DiscussPostItems discussPostItems =  leetcodeClient.fetchDiscussionPostItems();
    
      discussPostItems.ugcArticleDiscussionArticles.edges.forEach(edge -> {
        try {
          DiscussPostDetail discussPostDetail = leetcodeClient.fetchPostDetails(edge.node.topicId);
          System.out.println(discussPostDetail);
        } catch (Exception e) {
          System.err.println("Error: " + e.getMessage());
        }
      });

    } catch (Exception e) {
      System.err.println("XXX");
    }
  }
}
