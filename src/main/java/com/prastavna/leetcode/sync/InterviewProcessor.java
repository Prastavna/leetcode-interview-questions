package com.prastavna.leetcode.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prastavna.leetcode.models.DiscussPostDetail;
import com.prastavna.leetcode.models.DiscussPostItems;
import com.prastavna.leetcode.models.Interview;
import com.prastavna.leetcode.models.InterviewValidator;
import com.prastavna.leetcode.repositories.InterviewRepository;
import com.prastavna.leetcode.services.Leetcode;
import com.prastavna.leetcode.services.Openai;
import java.util.List;
import java.util.Optional;

public class InterviewProcessor {
  private final Leetcode leetcodeClient;
  private final Openai openaiClient;
  private final InterviewRepository repository;
  private final ObjectMapper mapper;

  public InterviewProcessor(
      Leetcode leetcodeClient,
      Openai openaiClient,
      InterviewRepository repository,
      ObjectMapper mapper) {
    this.leetcodeClient = leetcodeClient;
    this.openaiClient = openaiClient;
    this.repository = repository;
    this.mapper = mapper;
  }

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
      repository.append(interview);

      return ProcessingResult.success(
          topicId, "Saved to " + repository.getPath(), json);
    } catch (Exception ex) {
      String message = ex.getMessage() != null ? ex.getMessage() : ex.toString();
      return ProcessingResult.failed(topicId, message);
    }
  }

  public enum ProcessingStatus {
    SUCCESS,
    SKIPPED,
    FAILED
  }

  public record ProcessingResult(
      String topicId, ProcessingStatus status, String message, String json) {

    public static ProcessingResult success(String topicId, String message, String json) {
      return new ProcessingResult(topicId, ProcessingStatus.SUCCESS, message, json);
    }

    public static ProcessingResult skipped(String topicId, String message) {
      return new ProcessingResult(topicId, ProcessingStatus.SKIPPED, message, null);
    }

    public static ProcessingResult failed(String topicId, String message) {
      return new ProcessingResult(topicId, ProcessingStatus.FAILED, message, null);
    }
  }
}
