package com.prastavna.leetcode.sync;

import com.prastavna.leetcode.config.Leetcode;
import com.prastavna.leetcode.models.Interview;
import com.prastavna.leetcode.repositories.InterviewRepository;
import com.prastavna.leetcode.repositories.LatestInterviewLocator;
import com.prastavna.leetcode.sync.InterviewProcessor.ProcessingResult;
import com.prastavna.leetcode.sync.InterviewProcessor.ProcessingStatus;
import com.prastavna.leetcode.utils.Date;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class InterviewSyncRunner {
  private final DiscussionPostCollector collector;
  private final InterviewProcessor processor;
  private final InterviewRepository repository;
  private final LatestInterviewLocator latestInterviewLocator;
  private final int concurrency;

  public InterviewSyncRunner(
      DiscussionPostCollector collector,
      InterviewProcessor processor,
      InterviewRepository repository,
      LatestInterviewLocator latestInterviewLocator,
      int concurrency) {
    this.collector = collector;
    this.processor = processor;
    this.repository = repository;
    this.latestInterviewLocator = latestInterviewLocator;
    this.concurrency = Math.max(1, concurrency);
  }

  public void run() throws IOException {
    LocalDate cutoff = LocalDate.now().minusDays(Leetcode.LAG_DAYS);
    LocalDate startDate = LocalDate.parse(Leetcode.FETCH_START_DATE);
    Optional<LocalDate> latestStored = latestInterviewLocator.findLatestInterviewDate();

    List<com.prastavna.leetcode.models.DiscussPostItems.Node> nodes =
        new ArrayList<>(collector.collectEligibleNodes(startDate, cutoff, latestStored));

    if (nodes.isEmpty()) {
      System.out.println("No eligible posts found for processing.");
      return;
    }

    // The next run resumes after the newest stored date, so posts are saved oldest-first and
    // nothing on or after the date of a retryable failure is saved; those posts get retried.
    nodes.sort(Comparator.comparing(node -> Date.toDate(node.createdAt)));

    System.out.println(
        "Processing " + nodes.size() + " posts with concurrency=" + concurrency);

    ExecutorService executor = Executors.newFixedThreadPool(concurrency);
    List<Future<ProcessingResult>> futures = new ArrayList<>();

    for (com.prastavna.leetcode.models.DiscussPostItems.Node node : nodes) {
      futures.add(executor.submit(() -> processor.process(node)));
    }

    List<Interview> pending = new ArrayList<>();
    String pendingDate = null;
    int saved = 0;
    int resolved = 0;
    int failures = 0;
    String retryFromDate = null;
    boolean interrupted = false;

    for (int i = 0; i < futures.size(); i++) {
      String date = Date.toDate(nodes.get(i).createdAt);
      if (!date.equals(pendingDate)) {
        // Every post from the previous date is resolved, so its interviews are safe to save.
        saved += saveAll(pending);
        pendingDate = date;
      }

      ProcessingResult result;
      try {
        result = futures.get(i).get();
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
        interrupted = true;
        break;
      } catch (ExecutionException ee) {
        Throwable cause = ee.getCause();
        String message = cause != null ? cause.getMessage() : ee.getMessage();
        System.err.println("Worker task failed: " + message);
        failures++;
        continue;
      }

      if (!handleResult(result)) {
        failures++;
      } else {
        resolved++;
      }

      if (result != null && result.status() == ProcessingStatus.RETRYABLE) {
        retryFromDate = date;
        pending.clear();
        break;
      }

      if (result != null && result.interview() != null) {
        pending.add(result.interview());
      }
    }

    if (retryFromDate == null && !interrupted) {
      saved += saveAll(pending);
    }

    executor.shutdownNow();
    try {
      executor.awaitTermination(60, TimeUnit.SECONDS);
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
      interrupted = true;
    }

    if (interrupted) {
      System.err.println("Processing interrupted before completion.");
    }

    System.out.println("Saved " + saved + " interviews to " + repository.getPath());
    if (retryFromDate != null) {
      System.out.println(
          "Stopped at a retryable failure; posts from " + retryFromDate + " onwards will be retried next run.");
    }

    if (resolved == 0 && failures > 0) {
      throw new IllegalStateException(
          "No posts could be processed (" + failures + " failed); aborting so the run is marked as failed.");
    }
    if (retryFromDate != null && saved == 0) {
      throw new IllegalStateException(
          "No progress: a retryable failure blocked every post; aborting so the run is marked as failed.");
    }
  }

  private int saveAll(List<Interview> interviews) throws IOException {
    for (Interview interview : interviews) {
      repository.append(interview);
    }
    int count = interviews.size();
    interviews.clear();
    return count;
  }

  /** Returns false if the post failed to process. */
  private boolean handleResult(ProcessingResult result) {
    if (result == null) {
      return true;
    }
    String topicId = result.topicId();
    if (result.status() == ProcessingStatus.SUCCESS) {
      if (result.json() != null) {
        System.out.println("Parsed interview JSON for topicId=" + topicId + ":\n" + result.json());
      }
      return true;
    }

    if (result.status() == ProcessingStatus.SKIPPED) {
      System.out.println("Skipping topicId=" + topicId + " due to " + result.message());
      return true;
    }

    System.err.println("Error processing topicId=" + topicId + ": " + result.message());
    return false;
  }
}
