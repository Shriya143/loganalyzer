package com.example.loganalyzer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class LogLoader implements CommandLineRunner {

    private static final String FILE = "C:/Users/PC/OneDrive/kaggle/access.log";
    private static final int MAX_LINES = 10000;

    // IP - - [date] "METHOD /path HTTP/1.1" status size
    private static final Pattern LINE = Pattern.compile(
            "^(\\S+) \\S+ \\S+ \\[([^ \\]]+) [^\\]]*\\] \"(\\S+) (\\S+)[^\"]*\" (\\d{3}) .*");

    private static final DateTimeFormatter IN =
            DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss", Locale.ENGLISH);
    private static final DateTimeFormatter OUT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JdbcTemplate jdbc;

    public LogLoader(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(String... args) throws Exception {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM LOG_ENTRY", Integer.class);
        if (count != null && count > 0) {
            System.out.println("Log already loaded: " + count + " rows");
            return;
        }

        int saved = 0;
        int read = 0;

        try (BufferedReader reader = Files.newBufferedReader(Path.of(FILE), StandardCharsets.ISO_8859_1)) {
            String line;
            while ((line = reader.readLine()) != null && read < MAX_LINES) {
                read++;
                Matcher m = LINE.matcher(line);
                if (!m.matches()) continue;   // skip broken lines

                try {
                    String timestamp = LocalDateTime.parse(m.group(2), IN).format(OUT);
                    String method = m.group(3);
                    String path = m.group(4);
                    int status = Integer.parseInt(m.group(5));

                    // Short name: first part of the path, e.g. /image or /product
                    String clean = path.split("\\?")[0];
                    String[] seg = clean.split("/");
                    String first = seg.length > 1 ? "/" + seg[1] : "/";

                    String level = status >= 500 ? "ERROR" : status >= 400 ? "WARN" : "INFO";
                    String message = status + " " + method + " " + first;

                    jdbc.update(
                            "INSERT INTO LOG_ENTRY (TIMESTAMP, LEVEL, MESSAGE) VALUES (?, ?, ?)",
                            timestamp, level, message);
                    saved++;
                } catch (Exception e) {
                    // skip this line
                }
            }
        }

        System.out.println("Loaded " + saved + " log lines into the database");
    }
}