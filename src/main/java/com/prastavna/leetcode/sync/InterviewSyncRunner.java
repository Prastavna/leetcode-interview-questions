package com.prastavna.leetcode.sync;

import com.prastavna.leetcode.config.Leetcode;
import com.prastavna.leetcode.repositories.LatestInterviewLocator;
import com.prastavna.leetcode.sync.InterviewProcessor.ProcessingResult;
import com.prastavna.leetcode.sync.InterviewProcessor.ProcessingStatus;
import java.time.LocalDate;
import java.util.ArrayList;
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
  private final LatestInterviewLocator latestInterviewLocator;
  private final int concurrency;

  public InterviewSyncRunner(
      DiscussionPostCollector collector,
      InterviewProcessor processor,
      LatestInterviewLocator latestInterviewLocator,
      int concurrency) {
    this.collector = collector;
    this.processor = processor;
    this.latestInterviewLocator = latestInterviewLocator;
    this.concurrency = Math.max(1, concurrency);
  }

  public void run() {
    LocalDate cutoff = LocalDate.now().minusDays(Leetcode.LAG_DAYS);
    LocalDate startDate = LocalDate.parse(Leetcode.FETCH_START_DATE);
    Optional<LocalDate> latestStored = latestInterviewLocator.findLatestInterviewDate();

    List<com.prastavna.leetcode.models.DiscussPostItems.Node> nodes =
        collector.collectEligibleNodes(startDate, cutoff, latestStored);

    if (nodes.isEmpty()) {
      System.out.println("No eligible posts found for processing.");
      return;
    }

    System.out.println(
        "Processing " + nodes.size() + " posts with concurrency=" + concurrency);

    ExecutorService executor = Executors.newFixedThreadPool(concurrency);
    List<Future<ProcessingResult>> futures = new ArrayList<>();

    for (com.prastavna.leetcode.models.DiscussPostItems.Node node : nodes) {
      futures.add(executor.submit(() -> processor.process(node)));
    }

    boolean interrupted = false;
    for (Future<ProcessingResult> future : futures) {
      try {
        ProcessingResult result = future.get();
        handleResult(result);
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
        interrupted = true;
        break;
      } catch (ExecutionException ee) {
        Throwable cause = ee.getCause();
        String message = cause != null ? cause.getMessage() : ee.getMessage();
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
  }

  private void handleResult(ProcessingResult result) {
    if (result == null) {
      return;
    }
    String topicId = result.topicId();
    if (result.status() == ProcessingStatus.SUCCESS) {
      if (result.json() != null) {
        System.out.println("Parsed interview JSON for topicId=" + topicId + ":\n" + result.json());
      }
      System.out.println(result.message());
      return;
    }

    if (result.status() == ProcessingStatus.SKIPPED) {
      System.out.println("Skipping topicId=" + topicId + " due to " + result.message());
      return;
    }

    System.err.println("Error processing topicId=" + topicId + ": " + result.message());
  }
}
