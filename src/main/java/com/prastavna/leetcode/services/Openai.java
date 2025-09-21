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
    IMPORTANT: DO NOT GENERATE ANYTHING THAT's NOT IN THE POST
    -----------------------------------
    You are given a LeetCode discussion post. Your job is to determine if it contains actual interview experiences with explicit questions. If it seems like that this post doesn't contain anything meaning full, just return null.

    Rules:
    1. If the post contains one or more explicit interview questions (DSA problems, coding tasks, system design, aptitude, HR, etc.), return a valid JSON object matching the `Interview` schema below.
    2. If the post only contains:
      - Requests for interview questions
      - General advice about interviews
      - Self-promotion or course links
      - Generic study guidance
      Then return `null`.
    3. Do not infer or hallucinate questions. Only use what is explicitly mentioned in the post.
    4. Company name, role, years of experience (yoe), and date must come directly from the post if present, otherwise leave them empty or default. Company name should not be something generic like `Company`, `MNC`, `Startup` et. Role can be empty but keep in mind that come compnaies like Microsoft have roles like L60, L61 etc. For YoE, if the post seems even a bit fishy return -1.
    5. Each interview round must contain only the explicit questions stated. Classify each question into the appropriate `QuestionType`. Sometimes for DSA questions will be give for example: Max area of Island etc. You need to figure out whether that is a DSA question or it would go to another cateogry.
    6. Output must strictly follow the `Interview` Java model structure.
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
