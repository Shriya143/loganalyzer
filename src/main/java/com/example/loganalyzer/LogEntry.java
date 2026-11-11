package com.example.loganalyzer;

import jakarta.persistence.*;

@Entity
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String timestamp;
    private String level;
    private String message;

    public LogEntry() {}

    public LogEntry(String timestamp, String level, String message) {
        this.timestamp = timestamp;
        this.level = level;
        this.message = message;
    }

    public Long getId() { return id; }
    public String getTimestamp() { return timestamp; }
    public String getLevel() { return level; }
    public String getMessage() { return message; }
}