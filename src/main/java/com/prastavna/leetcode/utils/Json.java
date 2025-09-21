package com.prastavna.leetcode.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class Json {

  private static final ObjectMapper MAPPER = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

  private Json() {}

  /**
   * Sorts a JSON array (represented as a string) by the provided key and returns a minified JSON string.
   *
   * @param jsonArray array payload to sort
   * @param key object field to sort by
   * @return the sorted array as a minified JSON string
   */
  public static String sortArrayByKey(String jsonArray, String key) {
    try {
      JsonNode root = MAPPER.readTree(jsonArray);
      if (!root.isArray()) {
        throw new IllegalArgumentException("Provided json is not an array");
      }

      ArrayNode sorted = sortArray((ArrayNode) root, key, true);
      return MAPPER.writeValueAsString(sorted);
    } catch (IOException ex) {
      throw new IllegalArgumentException("Unable to sort json array", ex);
    }
  }

  /**
   * Returns a new ArrayNode sorted by the provided key. Leaves the original instance untouched.
   *
   * @param array source array node
   * @param key object field to sort by
   * @param ascending whether to sort ascending (descending when false)
   * @return a new, sorted ArrayNode
   */
  public static ArrayNode sortArray(ArrayNode array, String key, boolean ascending) {
    if (array == null) {
      return MAPPER.createArrayNode();
    }

    List<JsonNode> items = new ArrayList<>();
    array.forEach(items::add);

    Comparator<JsonNode> comparator = Comparator.comparing(node -> extractComparable(node.get(key)), Json::compareValues);
    if (!ascending) {
      comparator = comparator.reversed();
    }

    items.sort(comparator);

    ArrayNode sorted = MAPPER.createArrayNode();
    items.forEach(sorted::add);
    return sorted;
  }

  public static ArrayNode sortArray(ArrayNode array, String key) {
    return sortArray(array, key, true);
  }

  private static ComparableWrapper extractComparable(JsonNode value) {
    if (value == null || value.isNull()) {
      return new ComparableWrapper(null, ComparableType.NULL);
    }
    if (value.isNumber()) {
      return new ComparableWrapper(value.decimalValue(), ComparableType.NUMBER);
    }
    if (value.isBoolean()) {
      return new ComparableWrapper(value.asBoolean(), ComparableType.BOOLEAN);
    }
    if (value.isTextual()) {
      return new ComparableWrapper(value.asText(""), ComparableType.TEXT);
    }
    return new ComparableWrapper(value.toString(), ComparableType.TEXT);
  }

  private static int compareValues(ComparableWrapper left, ComparableWrapper right) {
    if (left.type != right.type) {
      return Integer.compare(left.type.ordinal(), right.type.ordinal());
    }

    if (left.value == null && right.value == null) {
      return 0;
    }
    if (left.value == null) {
      return -1;
    }
    if (right.value == null) {
      return 1;
    }

    if (left.value instanceof String lStr && right.value instanceof String rStr) {
      return lStr.compareToIgnoreCase(rStr);
    }
    if (left.value instanceof Number lNum && right.value instanceof Number rNum) {
      return Double.compare(lNum.doubleValue(), rNum.doubleValue());
    }
    if (left.value instanceof Boolean lBool && right.value instanceof Boolean rBool) {
      return Boolean.compare(lBool, rBool);
    }
    return left.value.toString().compareTo(right.value.toString());
  }

  /**
   * Produces a minified JSON representation (no indentation or superfluous whitespace).
   *
   * @param json any valid JSON payload
   * @return the minified JSON string
   */
  public static String minify(String json) {
    try {
      JsonNode root = MAPPER.readTree(json);
      return MAPPER.writeValueAsString(root);
    } catch (JsonProcessingException ex) {
      throw new IllegalArgumentException("Invalid json payload", ex);
    }
  }

  private enum ComparableType {
    NULL,
    BOOLEAN,
    NUMBER,
    TEXT
  }

  private record ComparableWrapper(Object value, ComparableType type) {}
}
