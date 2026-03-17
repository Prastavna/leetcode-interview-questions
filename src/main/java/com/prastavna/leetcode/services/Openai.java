package com.prastavna.leetcode.services;

import java.util.Optional;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.StructuredChatCompletion;
import com.openai.models.chat.completions.StructuredChatCompletionCreateParams;
import com.prastavna.leetcode.models.Interview;

public class Openai {
  private final OpenAIClient openAIClient;
  private final String prompt = """
    You are given a LeetCode discussion post. Your task is to determine whether it contains **real interview experience details with explicitly mentioned questions**.

    ---

### **Decision Rules**

    1. **Return a valid JSON (Interview schema) ONLY if:**

      * The post clearly describes an interview experience, AND
      * Contains **at least one explicitly stated question** asked during the interview.

    2. **Return `null` if the post contains ONLY:**

      * Requests for questions
      * General interview advice or preparation tips
      * Study plans, resources, or links
      * Self-promotion or referrals
      * Vague descriptions without actual questions
      * Previously asked questions not tied to a real interview experience

    3. **Strict Extraction Policy**

      * DO NOT infer, rephrase, or assume questions.
      * ONLY extract **verbatim or clearly stated questions**.
      * If no concrete question is present → return `null`.

    ---

### **Field Extraction Rules**

    * **company**: Extract only if explicitly mentioned (e.g., Google, Amazon). Avoid generic terms like "MNC", "Startup".
    * **role**: Extract if present (e.g., SDE, L4, Backend Engineer). Otherwise, leave empty.
    * **yoe (years of experience)**:

      * Extract only if clearly stated.
      * If ambiguous or suspicious → return `-1`.
    * **date**: Extract if explicitly mentioned; otherwise leave empty.

    ---

### **Interview Rounds Handling**

    * Split the interview into rounds **only if the post distinguishes them** (e.g., Round 1, Online Assessment, HR Round).
    * If rounds are not clearly separated, group all questions into a **single round**.

    ---

### **Question Classification**

    For each extracted question, assign one of the following types:

    * **DSA** → Algorithms, data structures, coding problems (e.g., LeetCode-style)
    * **SystemDesign** → High-level or low-level system design
    * **Behavioral** → HR or experience-based questions
    * **Aptitude** → Logical reasoning, puzzles, quantitative questions
    * **Other** → Only if none of the above apply

    ---

### **Important Constraints**

    * Do NOT hallucinate missing details.
    * Do NOT include explanations or extra text.
    * Ensure output is **strictly valid JSON** matching the `Interview` schema.
    * If uncertain about validity → return `null`.

    ---

### **Output Format**

    * Return ONLY:

      * A valid `Interview` JSON object
        OR
      * `null`
""";

  public Openai(String openaiBaseUrl, String openaiApiKey) {
    openAIClient = OpenAIOkHttpClient.builder()
      .baseUrl(openaiBaseUrl)
      .apiKey(openaiApiKey)
      .build();
  }

  public Optional<Interview> getJsonCompletion(String msg) {
    StructuredChatCompletionCreateParams<Interview> params = StructuredChatCompletionCreateParams.<Interview>builder()
      .addSystemMessage(prompt)
      .addUserMessage(msg)
      .model(ChatModel.GPT_4O_MINI)
      .responseFormat(Interview.class)
      .build();
    
    StructuredChatCompletion<Interview> response =
        openAIClient.chat().completions().create(params);

    return response.choices().get(0).message().content();
  }
}
