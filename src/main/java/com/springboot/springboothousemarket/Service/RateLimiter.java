package com.springboot.springboothousemarket.Service;

import java.time.Duration;

public interface RateLimiter {

    boolean tryAcquire(String key, int limit, Duration window);
}
