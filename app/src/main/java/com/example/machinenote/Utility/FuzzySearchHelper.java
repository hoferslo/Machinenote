package com.example.machinenote.Utility;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FuzzySearchHelper {

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
        int[] prev = new int[b.length() + 1];
        int[] curr = new int[b.length() + 1];

        for (int j = 0; j <= b.length(); j++) {
            prev[j] = j;
        }

        for (int i = 1; i <= a.length(); i++) {
            curr[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                curr[j] = Math.min(
                        Math.min(curr[j - 1] + 1, prev[j] + 1),
                        prev[j - 1] + cost
                );
            }
            int[] temp = prev;
            prev = curr;
            curr = temp;
        }
        return prev[b.length()];
    }

    private static boolean tokenMatches(String haystackWord, String queryToken) {
        if (haystackWord.isEmpty() || queryToken.isEmpty()) return false;

        // Hitra primerjava podniza
        if (haystackWord.contains(queryToken)) return true;

        // Če je iskalni niz dolg, ne uporabljamo Levenshteina (predrago)
        if (queryToken.length() > 8) {
            return false;
        }

        int allowedErrors = Math.max(1, queryToken.length() / 4);
        int prefixLen = Math.min(haystackWord.length(), queryToken.length() + allowedErrors);
        String prefix = haystackWord.substring(0, prefixLen);

        return levenshtein(prefix, queryToken) <= allowedErrors
                || fuzzyContains(haystackWord, queryToken);
    }

    public static boolean fuzzyContains(String haystack, String needle) {
        if (needle.isEmpty()) return true;
        if (haystack.contains(needle)) return true;

        // Omejitev za zaščito pomnilnika
        if (needle.length() > 8 || haystack.length() > 50) return false;

        int allowedErrors = Math.max(1, needle.length() / 4);
        int windowSize = needle.length() + allowedErrors;

        for (int start = 0; start <= haystack.length() - needle.length(); start++) {
            int end = Math.min(haystack.length(), start + windowSize);
            String window = haystack.substring(start, end);
            if (levenshtein(window, needle) <= allowedErrors) {
                return true;
            }
        }
        return false;
    }

    public static boolean matchesAllTokens(String haystack, String query) {
        if (query.isEmpty()) return true;

        // Hitri "normalni" prehod - če se cel query nahaja v haystacku, ne rabimo razbijati na tokene
        if (haystack.contains(query)) return true;

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
                return false;
            }
        }
        return true;
    }

    @SafeVarargs
    public static <T> List<T> search(List<T> data, String query, Function<T, String>... fieldExtractors) {
        if (query == null || query.isEmpty()) {
            return data;
        }

        String normalizedQuery = normalizeText(query);

        return data.stream()
                .filter(item -> {
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