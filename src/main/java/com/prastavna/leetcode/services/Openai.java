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

  public Openai(String openaiBaseUrl, String openaiApiKey) {
    openAIClient = OpenAIOkHttpClient.builder()
      .baseUrl(openaiBaseUrl)
      .apiKey(openaiApiKey)
      .build();
  }

  public Optional<Interview> getJsonCompletion(String msg) {
    StructuredChatCompletionCreateParams<Interview> params = StructuredChatCompletionCreateParams.<Interview>builder()
      .addSystemMessage("You parse leetcode discussion post. You need to extract interview questions and return them in a specific json format. You need to figure out whether the post contains Interview questions or not. If it contains Interview questions, you should return a valid json format. If it doesn't contain Interview questions or someone is asking what type of questions come in the interview, you should return null.")
      .addUserMessage(msg)
      .model(ChatModel.GPT_4O_MINI)
      .responseFormat(Interview.class)
      .build();
    
    StructuredChatCompletion<Interview> response =
        openAIClient.chat().completions().create(params);

    return response.choices().get(0).message().content();
  }
}
