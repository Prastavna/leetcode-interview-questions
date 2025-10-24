package com.prastavna.leetcode.sync;

import com.prastavna.leetcode.models.DiscussPostItems;
import com.prastavna.leetcode.services.Leetcode;
import com.prastavna.leetcode.utils.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DiscussionPostCollector {
  private final Leetcode leetcodeClient;
  private final int pageSize;

  public DiscussionPostCollector(Leetcode leetcodeClient, int pageSize) {
    this.leetcodeClient = leetcodeClient;
    this.pageSize = pageSize;
  }

  public List<DiscussPostItems.Node> collectEligibleNodes(
      LocalDate startDate, LocalDate cutoffDate, Optional<LocalDate> latestStoredDate) {
    if (startDate == null || cutoffDate == null) {
      return Collections.emptyList();
    }

    List<DiscussPostItems.Node> nodes = new ArrayList<>();
    int skip = 0;
    boolean stop = false;

    while (true) {
      DiscussPostItems page;
      try {
        page = leetcodeClient.fetchDiscussionPostItems(skip, pageSize);
      } catch (Exception e) {
        System.err.println("Failed to fetch discussion posts: " + e.getMessage());
        break;
      }

      if (page == null
          || page.ugcArticleDiscussionArticles == null
          || page.ugcArticleDiscussionArticles.edges == null
          || page.ugcArticleDiscussionArticles.edges.isEmpty()) {
        break;
      }

      boolean anyAdded = false;
      for (DiscussPostItems.Edge edge : page.ugcArticleDiscussionArticles.edges) {
        if (edge == null || edge.node == null) {
          continue;
        }

        LocalDate createdAt = parseCreatedDate(edge.node.createdAt);
        if (createdAt == null) {
          continue;
        }

        if (latestStoredDate.isPresent()
            && (createdAt.isBefore(latestStoredDate.get())
                || createdAt.isEqual(latestStoredDate.get()))) {
          stop = true;
          break;
        }

        if (createdAt.isBefore(startDate)) {
          stop = true;
          break;
        }

        if (createdAt.isAfter(cutoffDate)) {
          continue;
        }

        nodes.add(edge.node);
        anyAdded = true;
      }

      if (stop) {
        break;
      }

      if (!anyAdded) {
        skip += pageSize;
        continue;
      }

      boolean hasNext =
          page.ugcArticleDiscussionArticles.pageInfo != null
              && page.ugcArticleDiscussionArticles.pageInfo.hasNextPage;
      if (!hasNext) {
        break;
      }
      skip += pageSize;
    }

    return nodes;
  }

  private LocalDate parseCreatedDate(String createdAtIso) {
    try {
      String normalized = Date.toDate(createdAtIso);
      return normalized == null ? null : LocalDate.parse(normalized);
    } catch (Exception ex) {
      return null;
    }
  }
}
