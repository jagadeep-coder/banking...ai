package com.hackathon.bankingai;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AskController {

    private final AiService aiService;

    public AskController(AiService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/ask")
    public String ask(@RequestParam String question,
                       @RequestParam(defaultValue = "en") String language) {
        return aiService.askAi(question, language);
    }

    @GetMapping("/insight")
    public String insight(@RequestParam(defaultValue = "en") String language) {
        return aiService.getDailyInsight(language);
    }

    @GetMapping("/explain-transaction")
    public String explainTransaction(@RequestParam(defaultValue = "en") String language) {
        return aiService.explainFlaggedTransaction(language);
    }
}