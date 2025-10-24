package com.prastavna.leetcode;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.prastavna.leetcode.config.Storage;
import com.prastavna.leetcode.repositories.InterviewRepository;
import com.prastavna.leetcode.repositories.JsonStorage;
import com.prastavna.leetcode.repositories.LatestInterviewLocator;
import com.prastavna.leetcode.services.Leetcode;
import com.prastavna.leetcode.services.Openai;
import com.prastavna.leetcode.sync.DiscussionPostCollector;
import com.prastavna.leetcode.sync.InterviewProcessor;
import com.prastavna.leetcode.sync.InterviewSyncRunner;
import io.github.cdimascio.dotenv.Dotenv;

public class App {
  public static void main(String[] args) {
    Dotenv dotenv = Dotenv.load();
    Openai openai = new Openai(dotenv.get("OPENAI_BASE_URL"), dotenv.get("OPENAI_API_KEY"));
    InterviewRepository repository = new JsonStorage(Storage.INTERVIEWS_JSON_PATH);
    Leetcode leetcodeClient = new Leetcode();
    ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    DiscussionPostCollector collector =
        new DiscussionPostCollector(leetcodeClient, com.prastavna.leetcode.config.Leetcode.PAGE_SIZE);
    InterviewProcessor processor =
        new InterviewProcessor(leetcodeClient, openai, repository, mapper);
    LatestInterviewLocator latestInterviewLocator = new LatestInterviewLocator(repository);

    int concurrency = resolveConcurrency(dotenv);
    InterviewSyncRunner runner =
        new InterviewSyncRunner(collector, processor, latestInterviewLocator, concurrency);

    try {
      runner.run();
    } catch (Exception e) {
      System.err.println("Error during interview sync: " + e.getMessage());
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
