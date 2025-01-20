package com.example.demo.controller;

import com.example.demo.service.DataLoadingService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.evaluation.RelevancyEvaluator;
import org.springframework.ai.model.Content;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RAGController_2 {

    private final ChatModel chatModel;
    private final ChatClient chatClient;
    private final DataLoadingService dataLoadingService;
    private final VectorStore vectorStore;

    private final String context = """
            Svetlana Yurievna Baskova (born 25 May 1965,[1] Moscow) is a Russian film director, screenwriter and painter.
            Svetlana Baskova[2] was born 25 May 1965 in Moscow. She graduated from the Moscow Architectural Institute in 1989. Since 1996 she has been making video and movies.[3]
                        
            Her first movie, Cocky - Running Doctor was filmed in 1998. The film was produced by renowned artist and director Oleg Mavromatti.
                        
            Baskova is perhaps most famous for her 1999 psychedelic exploitation horror film The Green Elephant. The film has gradually gained a cult following due to the large number of scenes of violence, necrophilia and coprophilia.[4][5] It has also become the subject of internet memes, fan-made music videos and YouTube poops.
                        
            Svetlana's next films: Five Bottles of Vodka and The Head became less surreal, but still contained a lot of dark humor, obscene language and psychedelic scenes.
                        
            In 2010, filming began for the film For Marx, about the struggle between the independent workers' trade union and the "New Russians".[6][7][8] The premiere screening took place at the "Khudozhestvenny" cinema in Ulyanovsk.
            
            Filmography
            Cocky — Running Doctor (1998)
            The Green Elephant (1999)
            Five Bottles of Vodka (2001)
            The Head (2003)
            Mozart (2006)
            Only Decision is Resistance (2011)
            For Marx (2012)
            """;

    public RAGController_2(ChatModel chatModel, DataLoadingService dataLoadingService, VectorStore vectorStore) {
        this.chatModel = chatModel;
        this.chatClient = ChatClient.builder(chatModel).build();;
        this.vectorStore = vectorStore;
        this.dataLoadingService = dataLoadingService;
    }

    @GetMapping("/rag/no-context")
    String noContext() {
        return chatClient
                .prompt()
                .user("What films did Svetlana Baskova created?")
                .call()
                .content();
    }

    @GetMapping("/rag/prompt-stuffing")
    String promptStuffing() {
        return chatClient
                .prompt()
                .user("What films did Svetlana Baskova created? Please consider this in your response: " +  context)
                .call()
                .content();
    }

    @GetMapping("/rag/test")
    String ragTest(@RequestParam(value = "message") String message) {
        var response = chatClient
                .prompt()
                .advisors(new QuestionAnswerAdvisor(vectorStore, SearchRequest.defaults()))
                .user(message)
                .call()
                .chatResponse();

        evaluate(message, response);

        return response.getResult().getOutput().getContent();
    }

    @GetMapping("/rag/load-data")
    void loadData() {
        dataLoadingService.load();
    }

    private void evaluate(String userText, ChatResponse response) {
        var relevancyEvaluator = new RelevancyEvaluator(ChatClient.builder(chatModel));

        EvaluationRequest evaluationRequest = new EvaluationRequest(userText,
                (List<Content>) response.getMetadata().get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS), response);

        EvaluationResponse evaluationResponse = relevancyEvaluator.evaluate(evaluationRequest);

        assert evaluationResponse.isPass();

        System.out.println("Evaluation passed");
    }
}
