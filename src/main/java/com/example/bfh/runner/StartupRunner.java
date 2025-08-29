package com.example.bfh.runner;

import com.example.bfh.config.AppProperties;
import com.example.bfh.dto.GenerateWebhookRequest;
import com.example.bfh.dto.GenerateWebhookResponse;
import com.example.bfh.model.Submission;
import com.example.bfh.repo.SubmissionRepository;
import com.example.bfh.service.SqlSolverService;
import com.example.bfh.service.WebhookClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartupRunner implements ApplicationRunner {

    private final AppProperties props;
    private final WebhookClient webhookClient;
    private final SqlSolverService solver;
    private final SubmissionRepository repo;

    @Override
    public void run(ApplicationArguments args) {
        var applicant = props.getApplicant();

        // 1) Generate webhook & token
        GenerateWebhookResponse resp = webhookClient.generate(
                props.getGenerateUrl(),
                new GenerateWebhookRequest(applicant.getName(), applicant.getRegNo(), applicant.getEmail())
        );
        if (resp == null || resp.getWebhook() == null || resp.getAccessToken() == null) {
            throw new IllegalStateException("Invalid response from generateWebhook API");
        }

        // 2) Solve and store result
        String finalSql = solver.loadFinalSqlFor(applicant.getRegNo());
        String qType = SqlSolverService.isEvenQuestion(applicant.getRegNo()) ? "Q2" : "Q1";
        Submission saved = repo.save(Submission.builder()
                .regNo(applicant.getRegNo())
                .questionType(qType)
                .finalQuery(finalSql)
                .createdAt(Instant.now())
                .build());
        log.info("Stored submission id={} for {}", saved.getId(), qType);

        // 3) Submit to returned webhook using JWT token in Authorization header
        webhookClient.submitFinalQuery(resp.getWebhook(), resp.getAccessToken(), finalSql);

        log.info("Flow completed ✅");
    }
}
