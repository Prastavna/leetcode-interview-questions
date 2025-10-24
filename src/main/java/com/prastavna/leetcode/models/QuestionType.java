package com.prastavna.leetcode.models;

public enum QuestionType {
  APTITUDE("Aptitude"),
  DEBUGGING("Debugging"),
  CULTURE_FIT("Culture Fit"),
  DSA("DSA"),
  GD("Group Discussion"),
  HLD("High Level Design"),
  HM("Hiring Manager"),
  HR("HR"),
  LANGUAGE_SPECIFIC("Language Specific"),
  LLD("Low Level Design"),
  MACHINE_CODING("Machine Coding"),
  OTHER("Other"),
  PROJECT_DISCUSSION("Project Discussion"),
  TAKE_HOME_ASSIGNMENT("Take Home Assignment"),
  TECHNICAL("Technical"),
  SCREENING("Screening"),
  SYSTEM_DESIGN("System Design");

  private final String label;

  QuestionType(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
