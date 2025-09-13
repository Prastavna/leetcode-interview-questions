package com.prastavna.leetcode.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Graphql {
  public static String getQuery(String path) {
    File file = new File(path);
    List<String> lines = new ArrayList<>();

    try (Scanner scanner = new Scanner(file)) {
      while (scanner.hasNextLine()) {
        lines.add(scanner.nextLine());
      }
      scanner.close();
    } catch (FileNotFoundException e) {
      System.err.println(e.getMessage());
    }

    return String.join("\n", lines);
  }
}
