package com.prastavna.leetcode.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.prastavna.leetcode.config.Storage;
import com.prastavna.leetcode.models.Interview;
import com.prastavna.leetcode.repositories.InterviewRepository;
import com.prastavna.leetcode.repositories.JsonStorage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Removes interviews whose {@code leetcodeId} matches any of the provided comma separated IDs.
 */
public final class InterviewCleanup {

  private InterviewCleanup() {}

  public static void main(String[] args) {
    try {
      execute(args);
    } catch (Exception ex) {
      System.err.println("Failed to clean interviews: " + ex.getMessage());
      System.exit(1);
    }
  }

  private static void execute(String[] args) throws IOException {
    String rawInput = (args != null && args.length > 0) ? args[0] : null;
    if (rawInput == null || rawInput.isBlank()) {
      rawInput = System.getenv("LEETCODE_IDS");
    }
    if (rawInput == null || rawInput.isBlank()) {
      throw new IllegalArgumentException("Missing comma separated LeetCode ID list.");
    }

    Set<String> targetIds = parseIds(rawInput);
    if (targetIds.isEmpty()) {
      throw new IllegalArgumentException("No valid LeetCode IDs were provided.");
    }

    InterviewRepository repository = new JsonStorage(Storage.INTERVIEWS_JSON_PATH);
    List<Interview> interviews = repository.readAll();
    if (interviews.isEmpty()) {
      System.out.println("No interviews found in " + repository.getPath());
      return;
    }

    List<Interview> remaining = new ArrayList<>();
    Set<String> matchedIds = new LinkedHashSet<>();

    for (Interview interview : interviews) {
      if (interview == null) continue;
      String leetcodeId = normalize(interview.getLeetcodeId());
      if (!leetcodeId.isEmpty() && targetIds.contains(leetcodeId)) {
        matchedIds.add(leetcodeId);
        continue;
      }
      remaining.add(interview);
    }

    if (matchedIds.isEmpty()) {
      System.out.println("No interviews matched the provided IDs. Nothing to remove.");
      return;
    }

    // Sort descending by date to match ingestion output before writing.
    remaining.sort(
        Comparator.comparing(
                InterviewCleanup::normalizedDate,
                Comparator.nullsLast(Comparator.naturalOrder()))
            .reversed());

    writeResult(repository.getPath(), remaining);

    System.out.printf(
        "Removed %d interview(s). Remaining entries: %d%n", matchedIds.size(), remaining.size());

    if (matchedIds.size() < targetIds.size()) {
      Set<String> missing = new LinkedHashSet<>(targetIds);
      missing.removeAll(matchedIds);
      System.out.println("No interview found for IDs: " + String.join(", ", missing));
    }
  }

  private static Set<String> parseIds(String rawInput) {
    Set<String> ids = new LinkedHashSet<>();
    Arrays.stream(rawInput.split(","))
        .map(InterviewCleanup::normalize)
        .filter(id -> !id.isEmpty())
        .forEach(ids::add);
    return ids;
  }

  private static String normalize(String value) {
    return value == null ? "" : value.trim();
  }

  private static void writeResult(Path outputPath, List<Interview> payload) throws IOException {
    if (outputPath == null) {
      throw new IllegalStateException("Unable to resolve interviews.json output path.");
    }

    if (outputPath.getParent() != null) {
      Files.createDirectories(outputPath.getParent());
    }

    ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    mapper.writeValue(outputPath.toFile(), payload);
  }

  private static String normalizedDate(Interview interview) {
    if (interview == null) return null;
    String date = normalize(interview.getDate());
    return date.isEmpty() ? null : date;
  }
}
