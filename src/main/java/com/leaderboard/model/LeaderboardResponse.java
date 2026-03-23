package com.leaderboard.model;

import java.util.List;

public class LeaderboardResponse {
    private List<UserProgress> users;
    private List<String> sections;
    private String refreshStatus;
    private List<String> defaultSections;

    public LeaderboardResponse() {}

    public LeaderboardResponse(List<UserProgress> users, List<String> sections, String refreshStatus) {
        this.users = users;
        this.sections = sections;
        this.refreshStatus = refreshStatus;
    }

    public List<UserProgress> getUsers() { return users; }
    public void setUsers(List<UserProgress> users) { this.users = users; }

    public List<String> getSections() { return sections; }
    public void setSections(List<String> sections) { this.sections = sections; }

    public String getRefreshStatus() { return refreshStatus; }
    public void setRefreshStatus(String refreshStatus) { this.refreshStatus = refreshStatus; }

    public List<String> getDefaultSections() { return defaultSections; }
    public void setDefaultSections(List<String> defaultSections) { this.defaultSections = defaultSections; }
}
