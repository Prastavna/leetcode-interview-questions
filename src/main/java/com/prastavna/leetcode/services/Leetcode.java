package com.prastavna.leetcode.services;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.net.URI;
import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.prastavna.leetcode.models.DiscussPostDetail;
import com.prastavna.leetcode.models.DiscussPostItems;
import com.prastavna.leetcode.utils.Graphql;

public class Leetcode {
  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;

  public Leetcode() {
    this.httpClient = HttpClient.newHttpClient();
    this.objectMapper = new ObjectMapper()
        .setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  private <T> T executeQuery(String query, Map<String, Object> variables, Class<T> responseType) throws Exception {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("query", query);
    if (variables != null && !variables.isEmpty()) {
      requestBody.put("variables", variables);
    }

    String requestBodyJson = objectMapper.writeValueAsString(requestBody);

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(com.prastavna.leetcode.config.Leetcode.API_URL))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() == 200) {
      JsonNode root = objectMapper.readTree(response.body());
      JsonNode dataNode = root.get("data");
      T responseJson = objectMapper.treeToValue(dataNode, responseType);
      return responseJson;
    }

    throw new RuntimeException("HTTP Error: " + response.statusCode() + " - " + response.body());
  }

  public DiscussPostItems fetchDiscussionPostItems() throws IOException, InterruptedException {
    return fetchDiscussionPostItems(0, com.prastavna.leetcode.config.Leetcode.PAGE_SIZE);
  }

  public DiscussPostItems fetchDiscussionPostItems(int skip, int first) throws IOException, InterruptedException {
    String query = Graphql.getQuery("src/main/java/com/prastavna/leetcode/queries/discussion_post_items.gql");
    Map<String, Object> variables = new HashMap<>();

    variables.put("orderBy", "MOST_RECENT");
    variables.put("keywords", new String[] {});
    variables.put("tagSlugs", new String[] {"interview"});
    variables.put("skip", skip);
    variables.put("first", first);

    DiscussPostItems discussPostItems = new DiscussPostItems();

    try {
      discussPostItems = executeQuery(query, variables, DiscussPostItems.class);
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
    }

    return discussPostItems;
  }

  public DiscussPostDetail fetchPostDetails(String topicId) throws IOException, InterruptedException {
    String query = Graphql.getQuery("src/main/java/com/prastavna/leetcode/queries/post_details.gql");
    Map<String, Object> variables = new HashMap<>();

    variables.put("topicId", topicId);

    DiscussPostDetail discussPostDetail = new DiscussPostDetail();

    try {
      discussPostDetail = executeQuery(query, variables, DiscussPostDetail.class);
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
    }

    return discussPostDetail;
}
}
