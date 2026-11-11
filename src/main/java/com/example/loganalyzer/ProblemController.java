package com.example.loganalyzer;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ProblemController {

    private final SolutionRepository solutionRepository;
    private final UserProblemRepository userProblemRepository;

    public ProblemController(SolutionRepository solutionRepository,
                             UserProblemRepository userProblemRepository) {
        this.solutionRepository = solutionRepository;
        this.userProblemRepository = userProblemRepository;
    }

    /**
     * Submit a user problem, find the best matching solution via keywords,
     * and save the record as SOLVED or UNSOLVED.
     */
    @PostMapping("/problems")
    public ResponseEntity<Map<String, Object>> submitProblem(@RequestBody Map<String, String> payload) {
        Map<String, Object> response = new HashMap<>();
        String problemText = payload != null ? payload.get("problemText") : null;

        // Validation: not empty and at most 1000 characters
        if (problemText == null || problemText.trim().isEmpty()) {
            response.put("error", "problemText cannot be empty.");
            return ResponseEntity.badRequest().body(response);
        }

        String trimmed = problemText.trim();
        if (trimmed.length() > 1000) {
            response.put("error", "problemText cannot exceed 1000 characters (length: " + trimmed.length() + ").");
            return ResponseEntity.badRequest().body(response);
        }

        // Search for best matching Solution (case-insensitive keyword matching)
        String lowerInput = trimmed.toLowerCase(Locale.ROOT);
        List<Solution> allSolutions = solutionRepository.findAll();

        Solution bestMatch = null;
        int highestScore = 0;

        for (Solution sol : allSolutions) {
            int score = calculateMatchScore(lowerInput, sol.getKeywords());
            if (score > highestScore) {
                highestScore = score;
                bestMatch = sol;
            }
        }

        if (highestScore > 0 && bestMatch != null) {
            UserProblem solvedProblem = new UserProblem(trimmed, "SOLVED", bestMatch.getId());
            UserProblem saved = userProblemRepository.save(solvedProblem);

            response.put("status", "SOLVED");
            response.put("problemId", saved.getId());
            response.put("matched", true);
            response.put("solution", bestMatch);
            return ResponseEntity.ok(response);
        } else {
            UserProblem unsolvedProblem = new UserProblem(trimmed, "UNSOLVED", null);
            UserProblem saved = userProblemRepository.save(unsolvedProblem);

            response.put("status", "UNSOLVED");
            response.put("problemId", saved.getId());
            response.put("matched", false);
            response.put("message", "No fix yet. Our engineering team has logged this unsolved error and will review it shortly.");
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Calculate keyword matching score between the problem text and a solution's keywords.
     */
    private int calculateMatchScore(String lowerText, String rawKeywords) {
        if (rawKeywords == null || rawKeywords.trim().isEmpty()) {
            return 0;
        }

        int score = 0;
        String[] keywords = rawKeywords.split(",");

        for (String kw : keywords) {
            String phrase = kw.trim().toLowerCase(Locale.ROOT);
            if (phrase.isEmpty()) continue;

            // Full phrase containment match
            if (lowerText.contains(phrase)) {
                // Award higher weight for longer, multi-word specific phrases
                int wordCount = phrase.split("\\s+").length;
                score += 15 + (wordCount * 10) + phrase.length();
            } else {
                // Token-level matching for individual keywords
                String[] tokens = phrase.split("[\\s_.,:;()/\\[\\]-]+");
                for (String token : tokens) {
                    if (token.length() >= 3 && lowerText.contains(token)) {
                        score += 5 + token.length();
                    }
                }
            }
        }

        return score;
    }

    /**
     * List all unsolved problems.
     */
    @GetMapping("/problems/unsolved")
    public List<UserProblem> getUnsolvedProblems() {
        return userProblemRepository.findByStatusOrderByCreatedAtDesc("UNSOLVED");
    }

    /**
     * Get statistics on solved vs unsolved problems.
     */
    @GetMapping("/problems/stats")
    public Map<String, Object> getProblemStats() {
        long solved = userProblemRepository.countByStatus("SOLVED");
        long unsolved = userProblemRepository.countByStatus("UNSOLVED");
        long total = solved + unsolved;

        Map<String, Object> stats = new HashMap<>();
        stats.put("solved", solved);
        stats.put("unsolved", unsolved);
        stats.put("total", total);
        return stats;
    }

    /**
     * Allow an admin to add a new solution, optionally resolving an unsolved problem.
     * Uses JPA with parameterized queries.
     */
    @PostMapping("/solutions")
    public ResponseEntity<Map<String, Object>> addSolution(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();

        if (payload == null) {
            response.put("error", "Request body is required.");
            return ResponseEntity.badRequest().body(response);
        }

        String title = (String) payload.get("title");
        String keywords = (String) payload.get("keywords");
        String solutionText = (String) payload.get("solution");

        if (title == null || title.trim().isEmpty()) {
            response.put("error", "title is required.");
            return ResponseEntity.badRequest().body(response);
        }
        if (keywords == null || keywords.trim().isEmpty()) {
            response.put("error", "keywords is required.");
            return ResponseEntity.badRequest().body(response);
        }
        if (solutionText == null || solutionText.trim().isEmpty()) {
            response.put("error", "solution text is required.");
            return ResponseEntity.badRequest().body(response);
        }

        Solution newSolution = new Solution(keywords.trim(), title.trim(), solutionText.trim());
        Solution savedSolution = solutionRepository.save(newSolution);

        // Optionally link and resolve an existing unsolved problem
        Object problemIdObj = payload.get("problemId");
        Long problemId = null;
        if (problemIdObj != null) {
            try {
                problemId = Long.valueOf(problemIdObj.toString());
            } catch (NumberFormatException ignored) {}
        }

        if (problemId != null) {
            Optional<UserProblem> probOpt = userProblemRepository.findById(problemId);
            if (probOpt.isPresent()) {
                UserProblem prob = probOpt.get();
                prob.setStatus("SOLVED");
                prob.setSolutionId(savedSolution.getId());
                userProblemRepository.save(prob);
                response.put("problemResolved", true);
                response.put("resolvedProblemId", prob.getId());
            }
        }

        response.put("success", true);
        response.put("message", "Solution added successfully.");
        response.put("solution", savedSolution);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * List all solutions in the knowledge base.
     */
    @GetMapping("/solutions")
    public List<Solution> getAllSolutions() {
        return solutionRepository.findAll();
    }
}
