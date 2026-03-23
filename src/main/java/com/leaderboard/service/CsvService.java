package com.leaderboard.service;

import com.leaderboard.model.CsvUser;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class CsvService {

    private static final Logger logger = Logger.getLogger(CsvService.class.getName());
    // Default CSV file path - looks in current directory or classpath
    private static final String CSV_FILENAME = "users.csv";

    public List<CsvUser> loadUsers() {
        List<CsvUser> users = new ArrayList<>();

        // Try to load from working directory first
        Path csvPath = Paths.get(CSV_FILENAME);
        if (!Files.exists(csvPath)) {
            // Try loading from classpath resources
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(CSV_FILENAME)) {
                if (is != null) {
                    return parseStream(is);
                }
            } catch (IOException e) {
                logger.warning("Could not read CSV from classpath: " + e.getMessage());
            }
            logger.warning("CSV file not found at: " + csvPath.toAbsolutePath());
            return users;
        }

        try (InputStream is = Files.newInputStream(csvPath)) {
            return parseStream(is);
        } catch (IOException e) {
            logger.severe("Error reading CSV file: " + e.getMessage());
        }

        return users;
    }

    private List<CsvUser> parseStream(InputStream is) throws IOException {
        List<CsvUser> users = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                // Skip header row
                if (firstLine && line.toUpperCase().startsWith("USERID")) {
                    firstLine = false;
                    continue;
                }
                firstLine = false;
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    String userId = parts[0].trim();
                    String profileLink = parts[1].trim();
                    if (!userId.isEmpty() && !profileLink.isEmpty()) {
                        users.add(new CsvUser(userId, profileLink));
                    }
                }
            }
        }
        logger.info("Loaded " + users.size() + " users from CSV");
        return users;
    }

    public boolean saveCsvContent(String content) {
        try {
            Files.writeString(Paths.get(CSV_FILENAME), content);
            return true;
        } catch (IOException e) {
            logger.severe("Error saving CSV: " + e.getMessage());
            return false;
        }
    }

    public String readCsvContent() {
        Path csvPath = Paths.get(CSV_FILENAME);
        if (Files.exists(csvPath)) {
            try {
                return Files.readString(csvPath);
            } catch (IOException e) {
                logger.warning("Error reading CSV: " + e.getMessage());
            }
        }
        // Return sample content if file doesn't exist
        return "USERID,PROFILELINK\n# Add your users here\n";
    }
}
