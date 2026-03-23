package com.leaderboard.controller;

import com.leaderboard.model.LeaderboardResponse;
import com.leaderboard.model.UserProgress;
import com.leaderboard.service.LeaderboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    /**
     * GET /api/leaderboard - Returns current leaderboard state
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<LeaderboardResponse> getLeaderboard() {
        return ResponseEntity.ok(leaderboardService.getLeaderboard());
    }

    /**
     * POST /api/refresh - Triggers a fresh scrape of all users
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh() {
        leaderboardService.startRefresh();
        return ResponseEntity.ok(Map.of("status", "STARTED", "message", "Refresh initiated"));
    }

    /**
     * GET /api/refresh/status - Returns current refresh status
     */
    @GetMapping("/refresh/status")
    public ResponseEntity<Map<String, Object>> getRefreshStatus() {
        List<UserProgress> users = leaderboardService.getAllUserStatuses();
        String status = leaderboardService.getRefreshStatus();
        long done = users.stream().filter(u -> "DONE".equals(u.getStatus()) || "ERROR".equals(u.getStatus())).count();
        return ResponseEntity.ok(Map.of(
            "refreshStatus", status,
            "users", users,
            "total", users.size(),
            "completed", done
        ));
    }

    /**
     * GET /api/sections - Returns known sections
     */
    @GetMapping("/sections")
    public ResponseEntity<List<String>> getSections() {
        return ResponseEntity.ok(leaderboardService.getAllSections());
    }

    /**
     * GET /api/csv - Returns current CSV content
     */
    @GetMapping("/csv")
    public ResponseEntity<Map<String, String>> getCsv() {
        return ResponseEntity.ok(Map.of("content", leaderboardService.getCsvContent()));
    }

    /**
     * POST /api/csv - Save new CSV content
     */
    @PostMapping("/csv")
    public ResponseEntity<Map<String, Object>> saveCsv(@RequestBody Map<String, String> body) {
        String content = body.get("content");
        if (content == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "No content provided"));
        }
        boolean saved = leaderboardService.saveCsvContent(content);
        return ResponseEntity.ok(Map.of("success", saved, "message", saved ? "CSV saved successfully" : "Failed to save CSV"));
    }
}
