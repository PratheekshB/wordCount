package com.demo.wordcount.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WordCountService {
    private static final Logger logger = LoggerFactory.getLogger(WordCountService.class);

    private static final ConcurrentHashMap<String, Integer> wordCounts = new ConcurrentHashMap<>();

    public static void updateWordCount(String word) {
        try {
            synchronized (wordCounts) {
                wordCounts.merge(word, 1, Integer::sum);
            }
        } catch (Exception e) {
            logger.error("Error updating word count for word: {}", word, e);

        }
    }

    public static Map<String, Integer> getWordCounts() {
        try {
            synchronized (wordCounts) {
                return new ConcurrentHashMap<>(wordCounts);
            }
        } catch (Exception e) {
            logger.error("Error getting word counts", e);
            return Collections.emptyMap ( );
        }
    }


    public static Map<String, Integer> getWordNear(String word, int distanceThreshold) {
        try {
            synchronized (wordCounts) {
                Map<String, Integer> allWordCounts = new HashMap<> ( wordCounts );
                List<Map.Entry<String, Integer>> nearWords = allWordCounts.entrySet().stream()
                        .filter(entry -> isNear(word, entry.getKey(), distanceThreshold))
                        .toList ( );
                Map<String, Integer> result = new HashMap<>();
                for (Map.Entry<String, Integer> entry : nearWords) {
                    result.put(entry.getKey(), entry.getValue());
                }

                return result;
            }
        } catch (Exception e) {
            logger.error("Error getting words near '{}'", word, e);
            return Collections.emptyMap ( );
        }
    }
    private static boolean isNear(String word1, String word2, int distanceThreshold) {
        int len1 = word1.length();
        int len2 = word2.length();

        if (Math.abs(len1 - len2) > distanceThreshold) {
            return false;
        }

        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            for (int j = 0; j <= len2; j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = min(dp[i - 1][j - 1] + cost(word1.charAt(i - 1), word2.charAt(j - 1)),
                            dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1);
                }
            }
        }

        return dp[len1][len2] <= distanceThreshold;
    }

    private static int cost(char a, char b) {
        return a == b ? 0 : 1;
    }

    private static int min(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }
}
