package com.example.demo.controller;

import lombok.Data;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ChatClientTestController_1 {

    private final ChatClient chatClient;
    private final ChatClient systemChatClient;

    public ChatClientTestController_1(OpenAiChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.systemChatClient = ChatClient.builder(chatModel)
                .defaultSystem("You are a voice assistant that answers questions with voice: {voice}")
                .build();
    }

    @GetMapping("/chat/generate")
    public String generate1(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    @GetMapping("/chat/generateSystem")
    public String generateSystem2(@RequestParam(value = "voice", defaultValue = "Dad") String voice) {
        return systemChatClient.prompt()
                .user("Tell me a joke")
                .system(sp -> sp.param("voice", voice))
                .call()
                .content();
    }

    @GetMapping("/chat/generateFilms")
    public ActorFilms generateFilms3(@RequestParam(value = "actor", defaultValue = "Tom Hanks") String actor) {
        return chatClient.prompt()
                .user("What films did " + actor + " star in?")
                .call()
                .entity(ActorFilms.class);
    }

    @GetMapping("/chat/generateFilmsList")
    public List<ActorFilms> generateFilmsList4(@RequestParam(value = "actor", defaultValue = "Tom Hanks") List<String> actors) {
        return chatClient.prompt()
                .user("List filmographies for " + actors)
                .call()
                .entity(new ParameterizedTypeReference<>() {
                });
    }

    @Data
    public static class ActorFilms {
        String actor;
        List<String> films;
    }
}
