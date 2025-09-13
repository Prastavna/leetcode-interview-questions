package com.prastavna.leetcode.services;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.net.URI;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prastavna.leetcode.utils.Graphql;

public class Leetcode {
  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;

  public Leetcode() {
    this.httpClient = HttpClient.newHttpClient();
    this.objectMapper = new ObjectMapper();
  }

  private String executeQuery(String query, Map<String, Object> variables) throws Exception {
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
      return response.body();
    }

    throw new RuntimeException("HTTP Error: " + response.statusCode() + " - " + response.body());
  }

  public void fetchPostItems() throws IOException, InterruptedException {
    String query = Graphql.getQuery("src/main/java/com/prastavna/leetcode/queries/discussion_post_items.gql");
    Map<String, Object> variables = new HashMap<>();

    variables.put("orderBy", "MOST_RECENT");
    variables.put("keywords", new String[] {});
    variables.put("tagSlugs", new String[] {"interview"});
    variables.put("skip", 0);
    variables.put("first", com.prastavna.leetcode.config.Leetcode.PAGE_SIZE);

    try {
      String response = executeQuery(query, variables);
      System.out.println("Response: " + response);
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
    }
  }
}
