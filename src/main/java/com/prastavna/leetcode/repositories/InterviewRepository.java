package com.prastavna.leetcode.repositories;

import com.prastavna.leetcode.models.Interview;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface InterviewRepository {
  void append(Interview interview) throws IOException;

  List<Interview> readAll() throws IOException;

  Path getPath();
}
