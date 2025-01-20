package com.example.demo.controller;

import com.example.demo.service.MockWeatherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.function.FunctionCallbackWrapper;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
public class FunctionsController {

    private final ChatModel chatModel;

    public FunctionsController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/function-test")
    String functionTest() {
        return functionCallTest(OpenAiChatOptions.builder()
                .withModel(OpenAiApi.ChatModel.GPT_4_O.getValue())
                .withFunctionCallbacks(List.of(FunctionCallbackWrapper.builder(new MockWeatherService())
                        .withName("getCurrentWeather")
                        .withDescription("Get the weather in location")
                        .withResponseConverter((response) -> String.valueOf(response.temp()) + response.unit())
                        .build()))
                .build());
    }

    String functionCallTest(OpenAiChatOptions promptOptions) {
        UserMessage userMessage = new UserMessage("What's the weather like in San Francisco, Tokyo, and Paris?");

        List<Message> messages = new ArrayList<>(List.of(userMessage));

        ChatResponse response = chatModel.call(new Prompt(messages, promptOptions));

        log.info("Response: {}", response);

        return response.getResult().getOutput().getContent();
    }
}
