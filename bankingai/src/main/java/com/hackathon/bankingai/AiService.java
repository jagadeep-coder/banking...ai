package com.hackathon.bankingai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class AiService {

    private final RestClient restClient;

    private final String sampleTransactions = """
        1. Swiggy - Food - ₹450 - July 2
        2. Big Bazaar - Groceries - ₹1200 - July 4
        3. Uber - Transport - ₹300 - July 5
        4. Netflix - Entertainment - ₹500 - July 6
        5. Rent Payment - Housing - ₹15000 - July 1
        6. Zomato - Food - ₹600 - July 9
        7. Electricity Bill - Utilities - ₹1100 - July 10
        8. Amazon - Shopping - ₹2200 - July 12 (FLAGGED AS UNUSUAL - user normally spends under ₹800 on shopping)
        9. Starbucks - Food - ₹350 - July 14
        10. Petrol Pump - Transport - ₹1000 - July 15
        """;

    public AiService(@Value("${nvidia.api.key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl("https://integrate.api.nvidia.com/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    private String languageInstruction(String language) {
        return "en".equals(language)
                ? "Reply in simple, clear English."
                : "Reply only in Tamil (தமிழ்), using simple everyday words that an uneducated or elderly person can easily understand. Avoid English words and avoid complex banking jargon.";
    }

    @SuppressWarnings("unchecked")
    private String callAi(String systemPrompt, String userMessage) {
        Map<String, Object> requestBody = Map.of(
                "model", "meta/llama-3.1-8b-instruct",
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)
                )
        );

        Map<String, Object> response = restClient.post()
                .uri("/chat/completions")
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }

    public String askAi(String question, String language) {
        String systemPrompt = "You are a helpful banking assistant. Here are the user's recent transactions:\n"
                + sampleTransactions
                + "\nAnswer the user's question using this data. Be specific with numbers when possible. "
                + languageInstruction(language);
        return callAi(systemPrompt, question);
    }

    public String getDailyInsight(String language) {
        String systemPrompt = "You are a helpful banking assistant. Here are the user's recent transactions:\n"
                + sampleTransactions
                + "\nWrite ONE short, friendly insight (maximum 2 sentences) about the user's spending this month. "
                + "Mention a specific number or category. Make it feel personal and useful, like a smart banking app notification. "
                + languageInstruction(language);
        return callAi(systemPrompt, "Give me today's spending insight.");
    }

    public String explainFlaggedTransaction(String language) {
        String systemPrompt = "You are a fraud-detection assistant for a banking app. Here are the user's recent transactions:\n"
                + sampleTransactions
                + "\nOne transaction is marked FLAGGED AS UNUSUAL. Explain in 2-3 short sentences why it was flagged, "
                + "and suggest one simple action the user should take (e.g. confirm it was them, or report it if not). "
                + "Keep it calm and reassuring, not alarming. "
                + languageInstruction(language);
        return callAi(systemPrompt, "Explain the flagged transaction.");
    }
}