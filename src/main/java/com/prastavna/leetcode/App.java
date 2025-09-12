package com.prastavna.leetcode;

import com.prastavna.leetcode.utils.Graphql;

public class App {
  public static void main(String[] args) {
    String schema = Graphql.getSchema("src/main/java/com/prastavna/leetcode/queries/discussion_post_items.gql");
    System.out.println(schema);
  }
}
