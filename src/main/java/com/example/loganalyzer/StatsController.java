package com.example.loganalyzer;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class StatsController {

    private final JdbcTemplate jdbc;

    public StatsController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Link 1: how many INFO, WARN, ERROR
    @GetMapping("/levels")
    public List<Map<String, Object>> levels() {
        return jdbc.queryForList(
                "SELECT LEVEL, COUNT(*) AS TOTAL FROM LOG_ENTRY GROUP BY LEVEL");
    }

    // Link 2: most common errors
    @GetMapping("/top-errors")
    public List<Map<String, Object>> topErrors() {
        return jdbc.queryForList(
                "SELECT MESSAGE, COUNT(*) AS TOTAL FROM LOG_ENTRY " +
                        "WHERE LEVEL IN ('ERROR','WARN') GROUP BY MESSAGE ORDER BY TOTAL DESC LIMIT 10");
    }

    // Link 3: all log lines
    @GetMapping("/logs")
    public List<Map<String, Object>> logs() {
        return jdbc.queryForList(
                "SELECT * FROM LOG_ENTRY ORDER BY TIMESTAMP DESC LIMIT 100");
    }
}
