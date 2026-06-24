package com.isrs.optimised;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;
import java.util.TreeMap;

/**
 * Global call counter for benchmarking. Each method invocation
 * increments its counter. Provides totals for empirical comparison.
 */
public class CallCounter {

    private static final ConcurrentHashMap<String, AtomicInteger> counters = new ConcurrentHashMap<>();
    private static boolean enabled = false;

    public static void enable()  { enabled = true; }
    public static void disable() { enabled = false; }

    public static void reset() {
        counters.clear();
    }

    public static void count(String methodName) {
        if (!enabled) return;
        counters.computeIfAbsent(methodName, k -> new AtomicInteger(0)).incrementAndGet();
    }

    public static int getTotal() {
        return counters.values().stream().mapToInt(AtomicInteger::get).sum();
    }

    public static Map<String, Integer> getCounts() {
        Map<String, Integer> result = new TreeMap<>();
        counters.forEach((k, v) -> result.put(k, v.get()));
        return result;
    }
}
