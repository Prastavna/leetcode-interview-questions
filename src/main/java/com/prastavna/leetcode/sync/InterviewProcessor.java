package com.prastavna.leetcode.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.errors.InternalServerException;
import com.openai.errors.OpenAIIoException;
import com.openai.errors.OpenAIRetryableException;
import com.openai.errors.RateLimitException;
import com.prastavna.leetcode.models.DiscussPostDetail;
import com.prastavna.leetcode.models.DiscussPostItems;
import com.prastavna.leetcode.models.Interview;
import com.prastavna.leetcode.models.InterviewValidator;
import com.prastavna.leetcode.services.Leetcode;
import com.prastavna.leetcode.services.Openai;
import java.util.List;
import java.util.Optional;

public class InterviewProcessor {
  private final Leetcode leetcodeClient;
  private final Openai openaiClient;
  private final ObjectMapper mapper;

  public InterviewProcessor(Leetcode leetcodeClient, Openai openaiClient, ObjectMapper mapper) {
    this.leetcodeClient = leetcodeClient;
    this.openaiClient = openaiClient;
    this.mapper = mapper;
  }

  /** Parses a post into an interview. Saving is left to the caller so it can control ordering. */

  public ProcessingResult process(DiscussPostItems.Node node) {
    String topicId = node != null ? String.valueOf(node.topicId) : "<unknown>";
    if (node == null) {
      return ProcessingResult.failed(topicId, "Node payload is null");
    }

    try {
      DiscussPostDetail detail = leetcodeClient.fetchPostDetails(node.topicId);
      if (detail == null || detail.ugcArticleDiscussionArticle == null) {
        return ProcessingResult.failed(topicId, "Missing discussion detail");
      }

      String title =
          Optional.ofNullable(detail.ugcArticleDiscussionArticle.title).orElse("");
      String content =
          Optional.ofNullable(detail.ugcArticleDiscussionArticle.content).orElse("");

      Optional<Interview> interviewOpt = openaiClient.getJsonCompletion(title + " -- " + content);
      if (interviewOpt.isEmpty()) {
        return ProcessingResult.skipped(topicId, "No interview extracted");
      }

      Interview interview = interviewOpt.get();
      List<String> validationErrors = InterviewValidator.validate(interview);
      if (!validationErrors.isEmpty()) {
        return ProcessingResult.skipped(
            topicId, "Validation errors: " + String.join("; ", validationErrors));
      }

      interview.enrichFromLeetcode(
          String.valueOf(node.topicId), detail.ugcArticleDiscussionArticle.createdAt);

      String json = mapper.writeValueAsString(interview);
      return ProcessingResult.success(topicId, interview, json);
    } catch (Exception ex) {
      String message = ex.getMessage() != null ? ex.getMessage() : ex.toString();
      if (isTransient(ex)) {
        return ProcessingResult.retryable(topicId, message);
      }
      return ProcessingResult.failed(topicId, message);
    }
  }

  // Overload (503), rate limiting and network errors are worth retrying on a later run.
  private static boolean isTransient(Exception ex) {
    return ex instanceof InternalServerException
        || ex instanceof RateLimitException
        || ex instanceof OpenAIRetryableException
        || ex instanceof OpenAIIoException;
  }

  public enum ProcessingStatus {
    SUCCESS,
    SKIPPED,
    FAILED,
    RETRYABLE
  }

  public record ProcessingResult(
      String topicId, ProcessingStatus status, String message, String json, Interview interview) {

    public static ProcessingResult success(String topicId, Interview interview, String json) {
      return new ProcessingResult(topicId, ProcessingStatus.SUCCESS, "Parsed", json, interview);
    }

    public static ProcessingResult skipped(String topicId, String message) {
      return new ProcessingResult(topicId, ProcessingStatus.SKIPPED, message, null, null);
    }

    public static ProcessingResult failed(String topicId, String message) {
      return new ProcessingResult(topicId, ProcessingStatus.FAILED, message, null, null);
    }

    public static ProcessingResult retryable(String topicId, String message) {
      return new ProcessingResult(topicId, ProcessingStatus.RETRYABLE, message, null, null);
    }
  }
}
