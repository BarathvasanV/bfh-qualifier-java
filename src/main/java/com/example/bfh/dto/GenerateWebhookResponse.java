package com.example.bfh.dto;

import lombok.Data;

@Data
public class GenerateWebhookResponse {
    private String webhook;      // URL to submit final answer
    private String accessToken;  // JWT token
}
