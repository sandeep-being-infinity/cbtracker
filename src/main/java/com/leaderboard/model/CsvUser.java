package com.leaderboard.model;

public class CsvUser {
    private String userId;
    private String profileLink;

    public CsvUser() {}

    public CsvUser(String userId, String profileLink) {
        this.userId = userId;
        this.profileLink = profileLink;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getProfileLink() { return profileLink; }
    public void setProfileLink(String profileLink) { this.profileLink = profileLink; }
}
