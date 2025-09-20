package com.prastavna.leetcode;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.prastavna.leetcode.models.DiscussPostDetail;
import com.prastavna.leetcode.models.DiscussPostItems;
import com.prastavna.leetcode.models.Interview;
import com.prastavna.leetcode.models.InterviewValidator;
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
      LocalDate cutoff = LocalDate.now().minusDays(com.prastavna.leetcode.config.Leetcode.LAG_DAYS);
      LocalDate start = LocalDate.parse(com.prastavna.leetcode.config.Leetcode.FETCH_START_DATE);
      int skip = 0;
      int first = com.prastavna.leetcode.config.Leetcode.PAGE_SIZE;

      // Determine the latest stored interview date (if any)
      LocalDate latestStored = null;
      try {
        List<Interview> existing = storage.readAll();
        for (Interview it : existing) {
          if (it == null || it.getDate() == null || it.getDate().isBlank()) continue;
          try {
            LocalDate d = LocalDate.parse(it.getDate());
            if (latestStored == null || d.isAfter(latestStored)) {
              latestStored = d;
            }
          } catch (Exception ignore) {}
        }
      } catch (Exception ignore) {}

      List<DiscussPostItems.Node> toProcess = new ArrayList<>();
      boolean stop = false;

      while (true) {
        DiscussPostItems page = leetcodeClient.fetchDiscussionPostItems(skip, first);
        if (page == null || page.ugcArticleDiscussionArticles == null || page.ugcArticleDiscussionArticles.edges == null || page.ugcArticleDiscussionArticles.edges.isEmpty()) {
          break;
        }

        boolean anyAdded = false;
        for (DiscussPostItems.Edge edge : page.ugcArticleDiscussionArticles.edges) {
          String createdStr = com.prastavna.leetcode.utils.Date.toDate(edge.node.createdAt);
          LocalDate created;
          try { created = LocalDate.parse(createdStr); } catch (Exception ex) { continue; }

          // Stop if we've gone beyond our persistence frontier
          if (latestStored != null && (created.isBefore(latestStored) || created.isEqual(latestStored))) {
            stop = true;
            break;
          }

          // Stop if we've reached configured start date boundary
          if (created.isBefore(start)) {
            stop = true;
            break;
          }

          // Skip records within lag days (too recent)
          if (created.isAfter(cutoff)) {
            continue;
          }

          toProcess.add(edge.node);
          anyAdded = true;
        }

        if (stop) break;
        if (!anyAdded) {
          // No eligible records in this page; advance
          skip += first;
          continue;
        }

        // Continue paging to gather more until stop condition
        boolean hasNext = page.ugcArticleDiscussionArticles.pageInfo != null && page.ugcArticleDiscussionArticles.pageInfo.hasNextPage;
        if (!hasNext) break;
        skip += first;
      }

      // Process collected posts one-by-one via OpenAI and persist
      for (DiscussPostItems.Node node : toProcess) {
        try {
          DiscussPostDetail detail = leetcodeClient.fetchPostDetails(node.topicId);
          Optional<Interview> interviewOpt = openai.getJsonCompletion( detail.ugcArticleDiscussionArticle.title+ " -- "+ detail.ugcArticleDiscussionArticle.content);
          if (interviewOpt.isPresent()) {
            Interview itv = interviewOpt.get();
            List<String> validationErrors = InterviewValidator.validate(itv);
            if (!validationErrors.isEmpty()) {
              System.out.println("Skipping topicId=" + node.topicId + " due to validation errors: " + String.join("; ", validationErrors));
              continue;
            }
            itv.enrichFromLeetcode(String.valueOf(node.topicId), detail.ugcArticleDiscussionArticle.createdAt);
            String json = mapper.writeValueAsString(itv);
            System.out.println("Parsed interview JSON for topicId=" + node.topicId + ":\n" + json);
            storage.append(itv);
            System.out.println("Saved to " + storage.getPath());
          } else {
            System.out.println("No interview extracted for topicId=" + node.topicId);
          }
        } catch (Exception ex) {
          System.err.println("Error processing topicId=" + node.topicId + ": " + ex.getMessage());
        }
      }

    } catch (Exception e) {
      System.err.println("Error during fetch loop: " + e.getMessage());
    }
  }
}
