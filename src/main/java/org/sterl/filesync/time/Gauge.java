package org.sterl.filesync.time;

import java.time.Duration;

public class Gauge {
    private long time = System.currentTimeMillis();
    private Duration duration;

    public Gauge start() {
        time = System.currentTimeMillis();
        return this;
    }

    public Gauge stop() {
        duration = Duration.ofMillis(System.currentTimeMillis() - time);
        return this;
    }

    public long ms() {
        return duration == null ? 0 : duration.toMillis();
    }

    public long sec() {
        return duration == null ? 0 : duration.getSeconds();
    }

    public Duration get() {
        return duration;
    }
}