package com.somefrills.misc;

@SuppressWarnings("unused")
public final class Clock {
    private final long delay;
    private long begin;

    public Clock(long delay) {
        this.delay = delay;
    }

    public Clock() {
        this.delay = 0;
    }

    public long getTimePassed() {
        if (this.begin == 0L) return 0L;
        return System.currentTimeMillis() - begin;
    }

    public void update() {
        begin = System.currentTimeMillis();
    }

    public boolean ended() {
        if (this.begin == 0L) return false;
        return getTimePassed() >= delay;
    }

    public boolean ended(boolean reset) {
        if (this.begin == 0L) return false;
        long curr;
        if ((curr = System.currentTimeMillis()) - begin >= delay) {
            if (reset) begin = curr;
            return true;
        }
        return false;
    }

    public boolean ended(long delay) {
        if (this.begin == 0L) return false;
        return getTimePassed() >= delay;
    }

    public boolean ended(
            long delay,
            boolean reset
    ) {
        if (this.begin == 0L) return false;
        long curr;
        if ((curr = System.currentTimeMillis()) - begin >= delay) {
            if (reset) begin = curr;
            return true;
        }
        return false;
    }

    public void clear() {
        this.begin = 0L;
    }
}
