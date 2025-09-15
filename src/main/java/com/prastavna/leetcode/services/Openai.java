package com.prastavna.leetcode.services;

import java.util.Optional;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.StructuredChatCompletion;
import com.openai.models.chat.completions.StructuredChatCompletionCreateParams;
import com.prastavna.leetcode.models.Interview;

public class Openai {
  private final OpenAIClient openAIClient;

  public Openai(String openaiBaseUrl, String openaiApiKey) {
    openAIClient = OpenAIOkHttpClient.builder()
      .baseUrl(openaiBaseUrl)
      .apiKey(openaiApiKey)
      .build();
  }

  public void getJsonCompletion(String msg) {
    StructuredChatCompletionCreateParams<Interview> params = StructuredChatCompletionCreateParams.<Interview>builder()
      .addSystemMessage("You are a helpful assistant who would parse leetcode interview questions and return them in a specific json format")
      .addUserMessage(msg)
      .model(ChatModel.GPT_4O_MINI)
      .responseFormat(Interview.class)
      .build();
    
    StructuredChatCompletion<Interview> response =
        openAIClient.chat().completions().create(params);

    Optional<Interview> interview = response.choices().get(0).message().content();
    System.out.println(interview);

    System.out.println("Company: " + interview.get().getCompany());
    System.out.println("Role: " + interview.get().getRole());
    System.out.println("Rounds: " + interview.get().getRounds().size());
  }
}
