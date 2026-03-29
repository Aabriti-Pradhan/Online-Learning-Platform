package com.finalyearproject.fyp.service.serviceImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalyearproject.fyp.service.HuggingFaceService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class HuggingFaceServiceImpl implements HuggingFaceService {

    @Value("${huggingface.api.key}")
    private String apiKey;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // New OpenAI-compatible chat completions endpoint
    private static final String CHAT_URL =
            "https://router.huggingface.co/v1/chat/completions";

    // meta-llama/Llama-3.1-8B-Instruct with :hf-inference suffix routes to the free tier
    private static final String MODEL = "meta-llama/Llama-3.1-8B-Instruct:cerebras";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient   httpClient   = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    // Send prompt using OpenAI-compatible Chat Completions API

    @Override
    public String prompt(String userPrompt) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "model", MODEL,
                "messages", List.of(
                        Map.of("role", "user", "content", userPrompt)
                ),
                "max_tokens", 2000,
                "temperature", 0.3,
                "stream", false
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(CHAT_URL))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(120))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 503) {
            // Model loading — wait and retry once
            Thread.sleep(20000);
            response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
        }

        if (response.statusCode() != 200) {
            throw new RuntimeException("HuggingFace API error "
                    + response.statusCode() + ": " + response.body());
        }

        // Parse OpenAI-compatible response: choices[0].message.content
        JsonNode root = objectMapper.readTree(response.body());
        JsonNode content = root.path("choices").path(0)
                .path("message").path("content");
        if (!content.isMissingNode()) {
            return content.asText("");
        }
        throw new RuntimeException("Unexpected HuggingFace response: "
                + response.body());
    }

    // Extract text from PDF

    @Override
    public String extractPdfText(String relativePath) throws Exception {
        File file = Paths.get(uploadDir).resolve(relativePath)
                .normalize().toFile();
        if (!file.exists())
            throw new RuntimeException("PDF not found: " + relativePath);

        try (PDDocument doc = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);
            // Limit to 3000 chars to stay within token limit
            return text.length() > 3000 ? text.substring(0, 3000) : text;
        }
    }

    // Generate MCQ questions from text

    @Override
    public List<GeneratedQuestion> generateQuestionsFromText(
            String text, int count) throws Exception {

        String prompt = "You are a test creator. Based on the following content, "
                + "generate exactly " + count + " multiple choice questions.\n\n"
                + "CONTENT:\n" + text + "\n\n"
                + "RULES:\n"
                + "- Each question must have exactly 4 options: A, B, C, D\n"
                + "- Clearly mark the correct answer\n"
                + "- Use this EXACT format for EVERY question:\n\n"
                + "Q: [question text]\n"
                + "A: [option A]\n"
                + "B: [option B]\n"
                + "C: [option C]\n"
                + "D: [option D]\n"
                + "ANSWER: [A or B or C or D]\n\n"
                + "Generate all " + count + " questions now:";

        String raw = prompt(prompt);
        return parseQuestions(raw);
    }

    // Parse AI response into structured questions

    private List<GeneratedQuestion> parseQuestions(String raw) {
        List<GeneratedQuestion> questions = new ArrayList<>();
        String[] blocks = raw.split("(?=Q:)");

        for (String block : blocks) {
            block = block.trim();
            if (block.isEmpty() || !block.startsWith("Q:")) continue;
            try {
                String qText  = extract(block, "Q:",  "A:");
                String optA   = extract(block, "A:",  "B:");
                String optB   = extract(block, "B:",  "C:");
                String optC   = extract(block, "C:",  "D:");
                String optD   = extract(block, "D:",  "ANSWER:");
                String answer = extractAnswer(block);

                if (qText != null && optA != null && optB != null
                        && optC != null && optD != null && answer != null) {
                    questions.add(new GeneratedQuestion(
                            qText.trim(), optA.trim(), optB.trim(),
                            optC.trim(), optD.trim(), answer.trim()
                    ));
                }
            } catch (Exception ignored) {}
        }
        return questions;
    }

    private String extract(String block, String startKey, String endKey) {
        int start = block.indexOf(startKey);
        int end   = block.indexOf(endKey);
        if (start == -1 || end == -1 || end <= start) return null;
        return block.substring(start + startKey.length(), end).trim();
    }

    private String extractAnswer(String block) {
        Matcher m = Pattern.compile("ANSWER:\\s*([ABCD])",
                Pattern.CASE_INSENSITIVE).matcher(block);
        return m.find() ? m.group(1).toUpperCase() : null;
    }
}