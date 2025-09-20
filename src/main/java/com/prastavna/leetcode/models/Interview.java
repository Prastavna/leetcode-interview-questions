package com.prastavna.leetcode.models;

import java.util.List;
import java.util.ArrayList;

import com.prastavna.leetcode.utils.Date;
import com.prastavna.leetcode.utils.Uuid;

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

    // Enrich fields from LeetCode metadata and ensure IDs
    public void enrichFromLeetcode(String leetcodeTopicId, String createdAtIso) {
        // Always set canonical identifiers from LeetCode
        this.leetcodeId = leetcodeTopicId;
        this.date = Date.toDate(createdAtIso);
        this.id = Uuid.generate("i");

        if (this.rounds == null) {
            this.rounds = new ArrayList<>();
        }
        for (Round r : this.rounds) {
            if (r == null) continue;
            r.setId(Uuid.generate("r"));
            if (r.getQuestions() == null) {
                r.setQuestions(new ArrayList<>());
            }
            for (Question q : r.getQuestions()) {
                if (q == null) continue;
                q.setId(Uuid.generate("q"));
            }
        }
    }
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
    SYSTEM_DESIGN("System Design");

    private final String label;

    QuestionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
