package com.prastavna.leetcode;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.time.LocalDate;

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

      if (toProcess.isEmpty()) {
        System.out.println("No eligible posts found for processing.");
        return;
      }

      int concurrency = resolveConcurrency(dotenv);
      System.out.println("Processing " + toProcess.size() + " posts with concurrency=" + concurrency);

      ExecutorService executor = Executors.newFixedThreadPool(concurrency);
      List<Future<?>> futures = new ArrayList<>();

      for (DiscussPostItems.Node node : toProcess) {
        futures.add(executor.submit(() -> processNode(node, leetcodeClient, openai, storage, mapper)));
      }

      boolean interrupted = false;
      for (Future<?> future : futures) {
        try {
          future.get();
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          interrupted = true;
          break;
        } catch (ExecutionException ee) {
          Throwable cause = ee.getCause();
          String message = (cause != null && cause.getMessage() != null) ? cause.getMessage() : ee.getMessage();
          System.err.println("Worker task failed: " + message);
        }
      }

      executor.shutdown();
      try {
        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
          executor.shutdownNow();
        }
      } catch (InterruptedException ie) {
        executor.shutdownNow();
        Thread.currentThread().interrupt();
        interrupted = true;
      }

      if (interrupted) {
        System.err.println("Processing interrupted before completion.");
      }

    } catch (Exception e) {
      System.err.println("Error during fetch loop: " + e.getMessage());
    }
  }

  private static void processNode(
      DiscussPostItems.Node node,
      Leetcode leetcodeClient,
      Openai openai,
      JsonStorage storage,
      ObjectMapper mapper) {
    try {
      DiscussPostDetail detail = leetcodeClient.fetchPostDetails(node.topicId);
      if (detail == null || detail.ugcArticleDiscussionArticle == null) {
        System.err.println("Missing discussion detail for topicId=" + node.topicId);
        return;
      }

      String title = Optional.ofNullable(detail.ugcArticleDiscussionArticle.title).orElse("");
      String content = Optional.ofNullable(detail.ugcArticleDiscussionArticle.content).orElse("");
      Optional<Interview> interviewOpt = openai.getJsonCompletion(title + " -- " + content);
      if (interviewOpt.isPresent()) {
        Interview itv = interviewOpt.get();
        List<String> validationErrors = InterviewValidator.validate(itv);
        if (!validationErrors.isEmpty()) {
          System.out.println("Skipping topicId=" + node.topicId + " due to validation errors: " + String.join("; ", validationErrors));
          return;
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

  private static int resolveConcurrency(Dotenv dotenv) {
    String override = dotenv.get("OPENAI_CONCURRENCY");
    if (override != null && !override.isBlank()) {
      try {
        int parsed = Integer.parseInt(override.trim());
        if (parsed > 0) {
          return parsed;
        }
      } catch (NumberFormatException nfe) {
        System.err.println("Invalid OPENAI_CONCURRENCY value '" + override + "', falling back to default.");
      }
    }

    int cores = Runtime.getRuntime().availableProcessors();
    return Math.max(1, Math.min(cores, 4));
  }
}
