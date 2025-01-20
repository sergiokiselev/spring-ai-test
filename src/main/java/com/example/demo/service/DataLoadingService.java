package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class DataLoadingService {

    @Value("classpath:pdfs/1699860460530314.pdf")
    private Resource pdfResource;

    private final VectorStore vectorStore;

    public void load() {
        PagePdfDocumentReader reader = new PagePdfDocumentReader(this.pdfResource,
                PdfDocumentReaderConfig.builder()
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                                .withNumberOfBottomTextLinesToDelete(3)
                                .withNumberOfTopPagesToSkipBeforeDelete(1)
                                .build())
                        .withPageBottomMargin(1)
                        .build());

        var tokenTextSplitter = new TokenTextSplitter();

        List<Document> splitDocuments = tokenTextSplitter.split(reader.read());

        for (Document document : splitDocuments) {
            document.getMetadata().put("filename", pdfResource.getFilename());
            document.getMetadata().put("version", 1);
        }

        vectorStore.add(splitDocuments);
    }
}
