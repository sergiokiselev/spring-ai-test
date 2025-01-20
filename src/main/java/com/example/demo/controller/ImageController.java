package com.example.demo.controller;

import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.base64.Base64;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.ai.image.*;
import org.springframework.ai.stabilityai.*;

@RestController
public class ImageController {

    private final StabilityAiImageModel imageClient;

    public ImageController(StabilityAiImageModel imageClient) {
        this.imageClient = imageClient;
    }

    @GetMapping("/image-gen")
    public ResponseEntity<ByteArrayResource> imageGen(@RequestParam String message) {

        ImageOptions options = ImageOptionsBuilder.builder()
                .withModel("dall-e-3")
                .withHeight(1024)
                .withWidth(1024)
                .build();

        ImagePrompt imagePrompt = new ImagePrompt(message, options);
        ImageResponse response = imageClient.call(imagePrompt);
        String encodedImage = response.getResult().getOutput().getB64Json();
        byte[] decoded = Base64.decode(encodedImage);

        ByteArrayResource resource = new ByteArrayResource(decoded);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(resource.contentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename("whatever")
                                .build().toString())
                .body(resource);
//        return "redirect:" + imageUrl;
    }
}
