package com.scenic.ai.rag.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentParserService {

    public ParsedDocument parse(MultipartFile file) {
        String fileName = file.getOriginalFilename() == null ? "uploaded.txt" : file.getOriginalFilename();
        String docType = detectDocType(fileName);
        try {
            return new ParsedDocument(fileName, docType, parseBytes(file.getBytes(), docType));
        } catch (IOException exception) {
            throw new IllegalArgumentException("文档解析失败: " + exception.getMessage());
        }
    }

    public String parseBytes(byte[] bytes, String docType) throws IOException {
        return switch (docType) {
            case "markdown", "txt" -> new String(bytes, StandardCharsets.UTF_8);
            case "pdf" -> parsePdf(bytes);
            default -> throw new IllegalArgumentException("暂不支持的文档类型: " + docType);
        };
    }

    private String parsePdf(byte[] bytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(bytes)) {
            return new PDFTextStripper().getText(document);
        }
    }

    private String detectDocType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".md") || lower.endsWith(".markdown")) {
            return "markdown";
        }
        if (lower.endsWith(".txt")) {
            return "txt";
        }
        if (lower.endsWith(".pdf")) {
            return "pdf";
        }
        throw new IllegalArgumentException("仅支持 Markdown、TXT、PDF 文档");
    }

    public record ParsedDocument(String fileName, String docType, String text) {
    }
}
