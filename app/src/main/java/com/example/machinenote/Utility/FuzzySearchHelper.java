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

    private static boolean tokenMatches(String haystackWord, String queryToken) {
        if (haystackWord.isEmpty() || queryToken.isEmpty()) return false;

        // Exact / substring hitri primer
        if (haystackWord.contains(queryToken)) return true;

        // Fuzzy prefix: primerjamo queryToken samo z začetkom haystackWord
        // (dolžine queryToken + malo tolerance), ne s celo besedo
        int allowedErrors = Math.max(1, queryToken.length() / 4);
        int prefixLen = Math.min(haystackWord.length(), queryToken.length() + allowedErrors);
        String prefix = haystackWord.substring(0, prefixLen);

        return levenshtein(prefix, queryToken) <= allowedErrors
                || fuzzyContains(haystackWord, queryToken); // za primer, ko beseda ni na začetku
    }

    /**
     * fuzzyContains ostane kot prej (drseče okno + levenshtein) - uporablja se kot fallback.
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
     * Preveri, ali se cel query (lahko več besed) ujema z enim poljem (haystack).
     * Vsaka beseda iz query-ja mora najti ujemanje nekje v haystacku (AND logika).
     */
    public static boolean matchesAllTokens(String haystack, String query) {
        if (query.isEmpty()) return true;

        String[] haystackWords = haystack.split("\\s+");
        String[] queryTokens = query.split("\\s+");

        for (String qToken : queryTokens) {
            if (qToken.isEmpty()) continue;

            boolean found = false;
            for (String hWord : haystackWords) {
                if (tokenMatches(hWord, qToken)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false; // ta beseda iz query-ja se ni ujela nikjer -> ni zadetka
            }
        }
        return true;
    }

    /**
     * Generic search: išče po VEČ POLJIH hkrati (npr. artikel + dobavitelj),
     * tako da query besede lahko "razpadejo" po različnih poljih.
     */
    @SafeVarargs
    public static <T> List<T> search(List<T> data, String query, Function<T, String>... fieldExtractors) {
        if (query == null || query.isEmpty()) {
            return data;
        }

        String normalizedQuery = normalizeText(query);

        return data.stream()
                .filter(item -> {
                    // Združimo VSA polja v en skupni haystack, da "pnev ventil"
                    // lahko najde "pnev" v artiklu in "ventil" v opisu, ali oboje v enem polju.
                    StringBuilder combined = new StringBuilder();
                    for (Function<T, String> extractor : fieldExtractors) {
                        String value = extractor.apply(item);
                        if (value != null) {
                            combined.append(normalizeText(value)).append(" ");
                        }
                    }
                    return matchesAllTokens(combined.toString().trim(), normalizedQuery);
                })
                .collect(Collectors.toList());
    }
}