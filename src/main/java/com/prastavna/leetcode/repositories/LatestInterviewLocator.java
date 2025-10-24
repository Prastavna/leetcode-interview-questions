package com.prastavna.leetcode.repositories;

import com.prastavna.leetcode.models.Interview;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class LatestInterviewLocator {
  private final InterviewRepository repository;

  public LatestInterviewLocator(InterviewRepository repository) {
    this.repository = repository;
  }

  public Optional<LocalDate> findLatestInterviewDate() {
    try {
      List<Interview> interviews = repository.readAll();
      LocalDate latest = null;
      for (Interview interview : interviews) {
        if (interview == null || interview.getDate() == null || interview.getDate().isBlank()) {
          continue;
        }
        try {
          LocalDate parsed = LocalDate.parse(interview.getDate());
          if (latest == null || parsed.isAfter(latest)) {
            latest = parsed;
          }
        } catch (Exception ignore) {
          // ignore malformed date entries
        }
      }
      return Optional.ofNullable(latest);
    } catch (IOException ex) {
      return Optional.empty();
    }
  }
}
