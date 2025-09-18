package com.prastavna.leetcode.repositories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.prastavna.leetcode.models.Interview;

public class JsonStorage {
  private final Path path;
  private final ObjectMapper mapper;

  public JsonStorage(String filePath) {
    this.path = Paths.get(filePath);
    this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
  }

  private void ensureFile() throws IOException {
    if (Files.notExists(path)) {
      if (path.getParent() != null) {
        Files.createDirectories(path.getParent());
      }
      Files.writeString(path, "[]");
    }
  }

  private String keyFromInterview(Interview i) {
    if (i == null) return "";
    if (i.getLeetcodeId() != null && !i.getLeetcodeId().isBlank()) {
      return "leetcodeId:" + i.getLeetcodeId().trim();
    }
    String id = Objects.toString(i.getId(), "").trim();
    String date = Objects.toString(i.getDate(), "").trim();
    if (!id.isEmpty() && !date.isEmpty()) {
      return "id:" + id + "|date:" + date;
    }
    return "id:" + id;
  }

  private String keyFromNode(JsonNode node) {
    if (node == null || !node.isObject()) return "";
    JsonNode leetcodeId = node.get("leetcodeId");
    if (leetcodeId != null && !leetcodeId.asText("").isBlank()) {
      return "leetcodeId:" + leetcodeId.asText().trim();
    }
    String id = node.path("id").asText("").trim();
    String date = node.path("date").asText("").trim();
    if (!id.isEmpty() && !date.isEmpty()) {
      return "id:" + id + "|date:" + date;
    }
    return "id:" + id;
  }

  public synchronized void append(Interview interview) throws IOException {
    if (interview == null) return;
    ensureFile();

    JsonNode current;
    try {
      current = mapper.readTree(Files.readString(path));
    } catch (JsonProcessingException e) {
      // If the file is corrupted or not an array, reset to empty array
      current = mapper.createArrayNode();
    }

    ArrayNode array = (current != null && current.isArray())
        ? (ArrayNode) current
        : mapper.createArrayNode();

    String newKey = keyFromInterview(interview);
    boolean replaced = false;
    for (int i = 0; i < array.size(); i++) {
      String existingKey = keyFromNode(array.get(i));
      if (existingKey.equals(newKey)) {
        array.set(i, mapper.valueToTree(interview));
        replaced = true;
        break;
      }
    }
    if (!replaced) {
      array.add(mapper.valueToTree(interview));
    }

    Files.writeString(path, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(array));
  }

  public synchronized List<Interview> readAll() throws IOException {
    ensureFile();
    try {
      String content = Files.readString(path);
      if (content == null || content.isBlank()) {
        return new ArrayList<>();
      }
      return mapper.readValue(content, new TypeReference<List<Interview>>() {});
    } catch (Exception e) {
      return new ArrayList<>();
    }
  }

  public Path getPath() {
    return path;
  }
}
