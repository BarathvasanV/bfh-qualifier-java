package com.example.bfh.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "bfh")
public class AppProperties {
    @NotBlank
    private String generateUrl;
    private Applicant applicant = new Applicant();
    private SqlPaths sql = new SqlPaths();

    @Data
    public static class Applicant {
        @NotBlank private String name;
        @NotBlank private String regNo;
        @Email @NotBlank private String email;
    }

    @Data
    public static class SqlPaths {
        @NotBlank private String q1; // odd
        @NotBlank private String q2; // even
    }
}
