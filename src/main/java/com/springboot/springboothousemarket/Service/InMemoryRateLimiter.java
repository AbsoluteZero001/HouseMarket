package com.springboot.springboothousemarket.Service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 单机滑动窗口限流，默认 profile 使用，适合教学和本地运行。
 *
 * 键空间有上限：限流键现在同时包含客户端 IP，攻击者可以用伪造的 X-Forwarded-For
 * 不断制造新键，把这个 Map 撑爆。这里做了两件事：
 *   1) 空闲超过一个完整窗口的键会被回收；
 *   2) 键数量仍然超过上限时，淘汰最久未使用的键（近似 LRU）。
 */
@Service
@Profile("!redis")
public class InMemoryRateLimiter implements RateLimiter {

    /** 最多保留的限流键数量。 */
    private static final int MAX_KEYS = 20_000;

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    @Override
    public boolean tryAcquire(String key, int limit, Duration window) {
        long now = System.currentTimeMillis();
        long windowMillis = Math.max(window.toMillis(), 1L);
        long start = now - windowMillis;
        Window current = windows.computeIfAbsent(key, k -> new Window());
        current.windowMillis = windowMillis;
        boolean allowed;
        synchronized (current.hits) {
            while (!current.hits.isEmpty() && current.hits.peekFirst() < start) {
                current.hits.pollFirst();
            }
            allowed = current.hits.size() < limit;
            if (allowed) {
                current.hits.addLast(now);
            }
            current.lastAccess = now;
        }
        if (allowed) {
            evictIfNeeded(now);
        }
        return allowed;
    }

    /**
     * 键空间治理：先回收彻底空闲的键，仍然超上限时淘汰最久未使用的键。
     */
    private void evictIfNeeded(long now) {
        if (windows.size() <= MAX_KEYS) {
            return;
        }
        windows.entrySet().removeIf(entry -> {
            Window window = entry.getValue();
            synchronized (window.hits) {
                return window.hits.isEmpty() && now - window.lastAccess > window.windowMillis;
            }
        });
        if (windows.size() <= MAX_KEYS) {
            return;
        }
        while (windows.size() > MAX_KEYS) {
            String oldestKey = null;
            long oldestAccess = Long.MAX_VALUE;
            for (Map.Entry<String, Window> entry : windows.entrySet()) {
                long access = entry.getValue().lastAccess;
                if (access < oldestAccess) {
                    oldestAccess = access;
                    oldestKey = entry.getKey();
                }
            }
            if (oldestKey == null) {
                return;
            }
            windows.remove(oldestKey);
        }
    }

    private static final class Window {
        private final Deque<Long> hits = new ArrayDeque<>();
        private volatile long lastAccess = System.currentTimeMillis();
        private volatile long windowMillis = 60_000L;
    }
}
