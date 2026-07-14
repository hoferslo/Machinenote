package com.example.machinenote.Utility;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FuzzySearchHelper {

    /**
     * Lowercase, zamenja šumnike z navadnimi črkami in odstrani ločila.
     */
    public static String normalizeText(String input) {
        if (input == null) return "";
        String s = input.toLowerCase();
        s = s.replace('č', 'c').replace('ć', 'c')
                .replace('š', 's')
                .replace('ž', 'z')
                .replace('đ', 'd');
        s = s.replaceAll("[^a-z0-9\\s]", "");
        s = s.replaceAll("\\s+", " ").trim();
        return s;
    }

    private static int levenshtein(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[a.length()][b.length()];
    }

    /**
     * Fuzzy match "needle" znotraj "haystack" (oba morata biti že normalizirana).
     */
    public static boolean fuzzyContains(String haystack, String needle) {
        if (needle.isEmpty()) return true;
        if (haystack.contains(needle)) return true;

        int allowedErrors = Math.max(1, needle.length() / 4);
        int windowSize = needle.length() + allowedErrors;

        for (int start = 0; start <= haystack.length() - 1; start++) {
            int end = Math.min(haystack.length(), start + windowSize);
            if (end - start < needle.length() - allowedErrors) continue;
            String window = haystack.substring(start, end);
            if (levenshtein(window, needle) <= allowedErrors) {
                return true;
            }
        }
        return false;
    }

    /**
     * Splošna generic search funkcija - poda se ji lista, query in seznam
     * "field extractorjev" (funkcij, ki iz objekta izluščijo string, po katerem se išče).
     *
     * Primer uporabe:
     * List<RezervniDel> result = FuzzySearchHelper.search(
     *         currentData,
     *         currentSearchQuery,
     *         RezervniDel::getArtikel,
     *         RezervniDel::getArtikel_dolgi_text,
     *         d -> String.valueOf(d.getId()),
     *         d -> String.valueOf(d.getSkladišče()),
     *         RezervniDel::getDobavitelj
     * );
     */
    @SafeVarargs
    public static <T> List<T> search(List<T> data, String query, Function<T, String>... fieldExtractors) {
        if (query == null || query.isEmpty()) {
            return data;
        }

        String normalizedQuery = normalizeText(query);

        return data.stream()
                .filter(item -> {
                    for (Function<T, String> extractor : fieldExtractors) {
                        String value = extractor.apply(item);
                        if (value != null && fuzzyContains(normalizeText(value), normalizedQuery)) {
                            return true;
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }
}