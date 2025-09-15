package com.prastavna.leetcode.models;

import java.util.List;

public class Interview {
    private String id;
    private String leetcodeId;
    private String company;
    private String role;
    private int yoe;
    private List<Round> rounds;
    private String date;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLeetcodeId() { return leetcodeId; }
    public void setLeetcodeId(String leetcodeId) { this.leetcodeId = leetcodeId; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public int getYoe() { return yoe; }
    public void setYoe(int yoe) { this.yoe = yoe; }

    public List<Round> getRounds() { return rounds; }
    public void setRounds(List<Round> rounds) { this.rounds = rounds; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}

class Round {
    private String id;
    private List<Question> questions;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }
}

class Question {
    private String id;
    private QuestionType type;
    private String content;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public QuestionType getType() { return type; }
    public void setType(QuestionType type) { this.type = type; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}

enum QuestionType {
    APTITUDE("Aptitude"),
    DEBUGGING("Debugging"),
    DSA("DSA"),
    GD("Group Discussion"),
    HLD("High Level Design"),
    HM("Hiring Manager"),
    HR("HR"),
    LANGUAGE_SPECIFIC("Language Specific"),
    LLD("Low Level Design"),
    MACHINE_CODING("Machine Coding"),
    PROJECT_DISCUSSION("Project Discussion"),
    TAKE_HOME_ASSIGNMENT("Take Home Assignment"),
    TECHNICAL("Technical"),
    SYSTEM_DESIGN("System Design");

    private final String label;

    QuestionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
