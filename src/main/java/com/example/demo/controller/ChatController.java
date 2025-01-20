package com.example.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final OpenAiChatModel chatModel;

    private final VectorStore vectorStore;


    @GetMapping("/ai/add-document")
    public void addDocument(@RequestParam(value = "document") String document) {
        vectorStore.add(List.of(new Document(document)));
    }

    @GetMapping("/ai/generate")
    public Map generate(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return Map.of("generation", chatModel.call(message));
    }

    @GetMapping("/ai/generateStream")
    public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        return chatModel.stream(prompt);
    }

    @GetMapping("/ai/vector")
    public List<String> vector(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return vectorStore.similaritySearch(SearchRequest.query(message)
                        .withTopK(5))
                .stream()
                .map(Document::getContent)
                .collect(Collectors.toList());
//                .toList();
    }
}
