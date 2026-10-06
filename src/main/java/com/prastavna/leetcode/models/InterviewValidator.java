package com.prastavna.leetcode.models;

import java.util.ArrayList;
import java.util.List;

public final class InterviewValidator {

  private InterviewValidator() {}

  /**
   * Drops blank questions and the rounds left empty, so a stray empty round from the model
   * doesn't make an otherwise valid interview fail validation.
   */
  public static void removeEmptyEntries(Interview interview) {
    if (interview == null || interview.getRounds() == null) {
      return;
    }
    List<Round> rounds = new ArrayList<>();
    for (Round round : interview.getRounds()) {
      if (round == null || round.getQuestions() == null) {
        continue;
      }
      List<Question> questions = new ArrayList<>();
      for (Question question : round.getQuestions()) {
        if (question != null && question.getContent() != null && !question.getContent().isBlank()) {
          questions.add(question);
        }
      }
      if (!questions.isEmpty()) {
        round.setQuestions(questions);
        rounds.add(round);
      }
    }
    interview.setRounds(rounds);
  }

  public static List<String> validate(Interview interview) {
    List<String> errors = new ArrayList<>();
    if (interview == null) {
      errors.add("interview payload is null");
      return errors;
    }

    if (interview.getCompany() == null || interview.getCompany().isBlank()) {
      errors.add("company is missing");
    }

    if (interview.getYoe() < 0 || interview.getYoe() > 35) {
      errors.add("yoe must be greater than or equal to 0");
    }

    List<Round> rounds = interview.getRounds();
    if (rounds == null || rounds.isEmpty()) {
      errors.add("rounds must have at least one entry");
      return errors;
    }

    for (int i = 0; i < rounds.size(); i++) {
      Round round = rounds.get(i);
      if (round == null) {
        errors.add("round " + (i + 1) + " is null");
        continue;
      }

      List<Question> questions = round.getQuestions();
      if (questions == null || questions.isEmpty()) {
        errors.add("round " + (i + 1) + " must have at least one question");
        continue;
      }

      for (int j = 0; j < questions.size(); j++) {
        Question question = questions.get(j);
        if (question == null) {
          errors.add("round " + (i + 1) + " question " + (j + 1) + " is null");
          continue;
        }
        if (question.getContent() == null || question.getContent().isBlank()) {
          errors.add("round " + (i + 1) + " question " + (j + 1) + " content is missing");
        }
      }
    }

    return errors;
  }

  public static boolean isValid(Interview interview) {
    return validate(interview).isEmpty();
  }
}
