package com.leaderboard.service;

import com.leaderboard.model.CsvUser;
import com.leaderboard.model.LeaderboardResponse;
import com.leaderboard.model.UserProgress;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

@Service
public class LeaderboardService {

    private static final Logger logger = Logger.getLogger(LeaderboardService.class.getName());

    private final CsvService csvService;
    private final CodingBatScraper scraper;

    // In-memory state
    private final Map<String, UserProgress> progressMap = new ConcurrentHashMap<>();
    private volatile String refreshStatus = "IDLE"; // IDLE, RUNNING, DONE
    private final List<String> allSections = new CopyOnWriteArrayList<>();

    // Default sections shown (Recursion-1 and Recursion-2 by default)
    public static final List<String> DEFAULT_SELECTED_SECTIONS = List.of("Recursion-1", "Recursion-2");

    // Known CodingBat sections in order
    private static final List<String> KNOWN_SECTIONS = List.of(
        "Warmup-1", "Warmup-2",
        "String-1", "String-2", "String-3",
        "Array-1", "Array-2", "Array-3",
        "Logic-1", "Logic-2",
        "Map-1", "Map-2",
        "Recursion-1", "Recursion-2",
        "AP-1", "Functional-1", "Functional-2"
    );

    public LeaderboardService(CsvService csvService, CodingBatScraper scraper) {
        this.csvService = csvService;
        this.scraper = scraper;
        this.allSections.addAll(KNOWN_SECTIONS);
    }

    public LeaderboardResponse getLeaderboard() {
        List<UserProgress> users = new ArrayList<>(progressMap.values());
        users.sort((a, b) -> b.getTotalSolved() - a.getTotalSolved());
        LeaderboardResponse resp = new LeaderboardResponse(users, new ArrayList<>(allSections), refreshStatus);
        resp.setDefaultSections(DEFAULT_SELECTED_SECTIONS);
        return resp;
    }

    public void startRefresh() {
        if ("RUNNING".equals(refreshStatus)) {
            logger.info("Refresh already running, skipping.");
            return;
        }
        refreshStatus = "RUNNING";

        List<CsvUser> csvUsers = csvService.loadUsers();
        if (csvUsers.isEmpty()) {
            logger.warning("No users found in CSV.");
            refreshStatus = "DONE";
            return;
        }

        // Mark all users as INPROGRESS
        progressMap.clear();
        for (CsvUser u : csvUsers) {
            UserProgress up = new UserProgress(u.getUserId(), u.getProfileLink());
            up.setStatus("INPROGRESS");
            progressMap.put(u.getUserId(), up);
        }

        // Scrape asynchronously, bounded thread pool
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(csvUsers.size(), 5));
        List<Future<?>> futures = new ArrayList<>();

        for (CsvUser u : csvUsers) {
            futures.add(executor.submit(() -> {
                UserProgress up = progressMap.get(u.getUserId());
                if (up != null) {
                    scraper.scrapeUserProgress(up);
                    // Register any new sections discovered
                    up.getSectionScores().keySet().forEach(section -> {
                        if (!allSections.contains(section)) {
                            allSections.add(section);
                        }
                    });
                }
            }));
        }

        // Watch for completion in background
        Thread watchThread = new Thread(() -> {
            try {
                for (Future<?> f : futures) {
                    f.get();
                }
            } catch (Exception e) {
                logger.warning("Error during refresh: " + e.getMessage());
            } finally {
                executor.shutdown();
                refreshStatus = "DONE";
                logger.info("Refresh complete.");
            }
        });
        watchThread.setDaemon(true);
        watchThread.start();
    }

    public List<UserProgress> getAllUserStatuses() {
        List<UserProgress> users = new ArrayList<>(progressMap.values());
        users.sort((a, b) -> b.getTotalSolved() - a.getTotalSolved());
        return users;
    }

    public String getRefreshStatus() {
        return refreshStatus;
    }

    public List<String> getAllSections() {
        return new ArrayList<>(allSections);
    }

    public List<String> getDefaultSelectedSections() {
        return DEFAULT_SELECTED_SECTIONS;
    }

    public String getCsvContent() {
        return csvService.readCsvContent();
    }

    public boolean saveCsvContent(String content) {
        return csvService.saveCsvContent(content);
    }
}
