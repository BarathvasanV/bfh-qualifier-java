package com.example.bfh.service;

import com.example.bfh.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class SqlSolverService {

    private final AppProperties props;
    private final ResourceLoader resourceLoader;

    public static boolean isEvenQuestion(String regNo) {
        String digits = regNo.replaceAll("[^0-9]", "");
        if (digits.length() < 2) {
            int val = Integer.parseInt(digits);
            return val % 2 == 0;
        }
        int lastTwo = Integer.parseInt(digits.substring(digits.length() - 2));
        return lastTwo % 2 == 0;
    }

    public String loadFinalSqlFor(String regNo) {
        boolean even = isEvenQuestion(regNo);
        String path = even ? props.getSql().getQ2() : props.getSql().getQ1();
        String qType = even ? "Q2" : "Q1";
        log.info("Determined question type: {} based on regNo {}", qType, regNo);

        try {
            Resource res = resourceLoader.getResource(path);
            byte[] bytes = res.getContentAsByteArray();
            String sql = new String(bytes, StandardCharsets.UTF_8).trim();
            if (sql.isEmpty()) throw new IllegalStateException("SQL file is empty: " + path);
            return sql;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL from " + path + ": " + e.getMessage(), e);
        }
    }
}

