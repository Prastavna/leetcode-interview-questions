package com.prastavna.leetcode.services;

import com.fasterxml.jackson.databind.util.JSONWrappedObject;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

public class Openai {
  private final OpenAIClient openAIClient;

  public Openai(String openaiBaseUrl, String openaiApiKey) {
    openAIClient = OpenAIOkHttpClient.builder()
      .baseUrl(openaiBaseUrl)
      .apiKey(openaiApiKey)
      .build();
  }

  public void getJsonCompletion() {
    ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
      .addUserMessage("Hiii name 3 colors")
      .model(ChatModel.GPT_4O_MINI)
      .build();

    // ChatCompletion chatCompletion = openAIClient.chat().completions().create(params);
    openAIClient.chat().completions().create(params).choices().stream()
                .flatMap(choice -> choice.message().content().stream())
                .forEach(System.out::println);
  }
}
