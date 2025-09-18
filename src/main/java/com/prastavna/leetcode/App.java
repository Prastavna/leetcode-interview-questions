package com.prastavna.leetcode;

import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.prastavna.leetcode.models.DiscussPostDetail;
import com.prastavna.leetcode.models.DiscussPostItems;
import com.prastavna.leetcode.models.Interview;
import com.prastavna.leetcode.config.Storage;
import com.prastavna.leetcode.repositories.JsonStorage;
import com.prastavna.leetcode.services.Leetcode;
import com.prastavna.leetcode.services.Openai;

import io.github.cdimascio.dotenv.Dotenv;

public class App {
  public static void main(String[] args) {
    Dotenv dotenv = Dotenv.load();
    Openai openai = new Openai(dotenv.get("OPENAI_BASE_URL"), dotenv.get("OPENAI_API_KEY"));
    JsonStorage storage = new JsonStorage(Storage.INTERVIEWS_JSON_PATH);

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
              Interview itv = interviewOpt.get();
              itv.enrichFromLeetcode(
                String.valueOf(edge.node.topicId),
                discussPostDetail.ugcArticleDiscussionArticle.createdAt
              );
              String json = mapper.writeValueAsString(itv);
              System.out.println("Parsed interview JSON for topicId=" + edge.node.topicId + ":\n" + json);
              storage.append(itv);
              System.out.println("Saved to " + storage.getPath().toString());
            } catch (JsonProcessingException e) {
              System.err.println("Failed to serialize interview for topicId=" + edge.node.topicId + ": " + e.getMessage());
            } catch (Exception e) {
              System.err.println("Failed to persist interview for topicId=" + edge.node.topicId + ": " + e.getMessage());
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
