package com.leaderboard.service;

import com.leaderboard.model.UserProgress;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CodingBatScraper {

    private static final Logger logger = Logger.getLogger(CodingBatScraper.class.getName());

    // Matches "Recursion-1 factorial", "String-2 doubleChar", "Warmup-1 sleep" etc.
    private static final Pattern SECTION_PATTERN = Pattern.compile(
        "^([A-Za-z]+-\\d+)\\s+\\S+.*$"
    );

    /**
     * Scrapes a CodingBat "done" page.
     *
     * The page lists every solved problem as an anchor tag with href "/prob/p..."
     * and link text like "Recursion-1 factorial" or "String-2 doubleChar".
     * We count occurrences per section prefix to get section-wise scores.
     */
    public void scrapeUserProgress(UserProgress userProgress) {
        String url = userProgress.getProfileLink();
        try {
            logger.info("Scraping: " + url);
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36")
                    .referrer("https://codingbat.com/")
                    .timeout(20000)
                    .get();

            Map<String, Integer> sectionScores = new LinkedHashMap<>();
            int total = 0;

            // Primary method: every solved problem is an <a href="/prob/pXXXXXX">
            // with text like "Recursion-1 factorial"
            Elements probLinks = doc.select("a[href*=/prob/p]");
            for (Element link : probLinks) {
                String text = link.text().trim();
                Matcher m = SECTION_PATTERN.matcher(text);
                if (m.matches()) {
                    String section = m.group(1);
                    sectionScores.merge(section, 1, Integer::sum);
                    total++;
                }
            }

            // Fallback: parse "Code Badge Data" summary text if no problem links found
            // Format in page body: "String-1:5 Logic-1:3 Array-1:2 ..."
            if (sectionScores.isEmpty()) {
                String pageText = doc.body().text();
                Pattern badgePattern = Pattern.compile("([A-Za-z]+-\\d+):(\\d+)");
                Matcher bm = badgePattern.matcher(pageText);
                while (bm.find()) {
                    String section = bm.group(1);
                    int count = Integer.parseInt(bm.group(2));
                    if (count > 0) {
                        sectionScores.put(section, count);
                        total += count;
                    }
                }
            }

            userProgress.setSectionScores(sectionScores);
            userProgress.setTotalSolved(total);
            userProgress.setStatus("DONE");
            logger.info("Scraped " + userProgress.getUserId()
                    + " | total=" + total + " | sections=" + sectionScores);

        } catch (IOException e) {
            logger.warning("Error scraping " + url + ": " + e.getMessage());
            userProgress.setStatus("ERROR");
            userProgress.setErrorMessage("Failed to fetch: " + e.getMessage());
        }
    }
}
