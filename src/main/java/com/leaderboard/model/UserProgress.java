package com.leaderboard.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class UserProgress {
    private String userId;
    private String profileLink;
    private String status; // PENDING, INPROGRESS, DONE, ERROR
    private Map<String, Integer> sectionScores = new LinkedHashMap<>();
    private int totalSolved;
    private String errorMessage;

    public UserProgress() {}

    public UserProgress(String userId, String profileLink) {
        this.userId = userId;
        this.profileLink = profileLink;
        this.status = "PENDING";
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getProfileLink() { return profileLink; }
    public void setProfileLink(String profileLink) { this.profileLink = profileLink; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Map<String, Integer> getSectionScores() { return sectionScores; }
    public void setSectionScores(Map<String, Integer> sectionScores) { this.sectionScores = sectionScores; }

    public int getTotalSolved() { return totalSolved; }
    public void setTotalSolved(int totalSolved) { this.totalSolved = totalSolved; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
