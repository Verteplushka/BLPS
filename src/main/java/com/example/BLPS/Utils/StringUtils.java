package com.example.BLPS.Utils;

public class StringUtils {
    public static int levenshteinDistance(String s1, String s2) {
        int lenS1 = s1.length();
        int lenS2 = s2.length();
        int[][] dp = new int[lenS1 + 1][lenS2 + 1];

        for (int i = 0; i <= lenS1; i++) {
            for (int j = 0; j <= lenS2; j++) {
                if (i == 0) {
                    dp[i][j] = j; // Если строка 1 пуста, вставляем все символы из строки 2
                } else if (j == 0) {
                    dp[i][j] = i; // Если строка 2 пуста, удаляем все символы из строки 1
                } else {
                    dp[i][j] = Math.min(Math.min(
                                    dp[i - 1][j] + 1, // Удаление
                                    dp[i][j - 1] + 1), // Вставка
                            dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1)); // Замена
                }
            }
        }

        return dp[lenS1][lenS2];
    }
}

