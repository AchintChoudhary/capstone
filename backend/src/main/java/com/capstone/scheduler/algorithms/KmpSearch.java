package com.capstone.scheduler.algorithms;

import java.util.ArrayList;
import java.util.List;

public class KmpSearch {

    /**
     * Searches for occurrences of 'pattern' in 'text' using KMP algorithm.
     * Returns true if pattern is found.
     */
    public static boolean search(String text, String pattern) {
        if (text == null || pattern == null) return false;
        if (pattern.isEmpty()) return true;
        
        String cleanText = text.toLowerCase();
        String cleanPattern = pattern.toLowerCase();

        int[] lps = computeLPSArray(cleanPattern);
        int i = 0; // index for text
        int j = 0; // index for pattern

        while (i < cleanText.length()) {
            if (cleanPattern.charAt(j) == cleanText.charAt(i)) {
                j++;
                i++;
            }
            if (j == cleanPattern.length()) {
                return true; // Match found
            } else if (i < cleanText.length() && cleanPattern.charAt(j) != cleanText.charAt(i)) {
                if (j != 0) {
                    j = lps[j - 1];
                } else {
                    i++;
                }
            }
        }
        return false;
    }

    private static int[] computeLPSArray(String pattern) {
        int[] lps = new int[pattern.length()];
        int len = 0;
        int i = 1;
        lps[0] = 0;

        while (i < pattern.length()) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else {
                if (len != 0) {
                    len = lps[len - 1];
                } else {
                    lps[i] = 0;
                    i++;
                }
            }
        }
        return lps;
    }
}
