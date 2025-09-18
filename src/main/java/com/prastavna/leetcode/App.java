package com.prastavna.leetcode;

import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.prastavna.leetcode.models.DiscussPostDetail;
import com.prastavna.leetcode.models.DiscussPostItems;
import com.prastavna.leetcode.models.Interview;
import com.prastavna.leetcode.services.Leetcode;
import com.prastavna.leetcode.services.Openai;

import io.github.cdimascio.dotenv.Dotenv;

public class App {
  public static void main(String[] args) {
    Dotenv dotenv = Dotenv.load();
    Openai openai = new Openai(dotenv.get("OPENAI_BASE_URL"), dotenv.get("OPENAI_API_KEY"));

    Leetcode leetcodeClient = new Leetcode();
    ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    try {
      DiscussPostItems discussPostItems = leetcodeClient.fetchDiscussionPostItems();

      discussPostItems.ugcArticleDiscussionArticles.edges.forEach(edge -> {
        try {
          DiscussPostDetail discussPostDetail = leetcodeClient.fetchPostDetails(edge.node.topicId);

          Optional<Interview> interviewOpt = openai.getJsonCompletion(
              discussPostDetail.ugcArticleDiscussionArticle.content);

          if (interviewOpt.isPresent()) {
            try {
              String json = mapper.writeValueAsString(interviewOpt.get());
              System.out.println("Parsed interview JSON for topicId=" + edge.node.topicId + ":\n" + json);
            } catch (JsonProcessingException e) {
              System.err.println("Failed to serialize interview for topicId=" + edge.node.topicId + ": " + e.getMessage());
            }
          } else {
            System.out.println("No interview extracted for topicId=" + edge.node.topicId);
          }
        } catch (Exception e) {
          System.err.println("Error processing topicId=" + edge.node.topicId + ": " + e.getMessage());
        }
      });

    } catch (Exception e) {
      System.err.println("Error fetching discussion posts: " + e.getMessage());
    }
  }
}
