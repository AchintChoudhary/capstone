package com.capstone.scheduler.algorithms;

public class SlidingWindow {

    /**
     * Calculates the moving average of an array of data using a sliding window.
     * Useful for smoothing out task completion statistics (e.g., rolling weekly completions).
     */
    public static double[] calculateMovingAverage(int[] data, int windowSize) {
        if (data == null || data.length == 0 || windowSize <= 0) {
            return new double[0];
        }
        
        int n = data.length;
        double[] result = new double[n];
        double sum = 0;

        for (int i = 0; i < n; i++) {
            sum += data[i];
            
            // If window exceeds current index, subtract the element exiting the window
            if (i >= windowSize) {
                sum -= data[i - windowSize];
            }
            
            int currentWindowSize = Math.min(i + 1, windowSize);
            result[i] = sum / currentWindowSize;
        }

        return result;
    }

    /**
     * Calculates the rolling sum of data using a sliding window.
     */
    public static int[] calculateRollingSum(int[] data, int windowSize) {
        if (data == null || data.length == 0 || windowSize <= 0) {
            return new int[0];
        }

        int n = data.length;
        int[] result = new int[n];
        int sum = 0;

        for (int i = 0; i < n; i++) {
            sum += data[i];

            if (i >= windowSize) {
                sum -= data[i - windowSize];
            }

            result[i] = sum;
        }

        return result;
    }
}
