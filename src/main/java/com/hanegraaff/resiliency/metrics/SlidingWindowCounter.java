package com.hanegraaff.resiliency.metrics;

import java.time.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A sliding window counter that resets every so often. The period is expressed
 * in minutes when the object is created
 */
public class SlidingWindowCounter {
    private final AtomicInteger count;
    private LocalDateTime lastEvent;
    private final int intervalMin;

    /**
     * Creates a new Sliding Window Counter
     * @param intervalMin the length of the interval expressed in minutes.
     */
    public SlidingWindowCounter(int intervalMin){
        this.intervalMin = intervalMin;
        count = new AtomicInteger();
        lastEvent = now();
    }

    /**
     * Increments the counter for the current time period
     * @return The incremented counter value
     */
    public int increment(){
        if (pastPeriod(now())){
            lastEvent = now();
            count.set(1);
            return count.get();
        }
        else{
            return count.addAndGet(1);
        }
    }

    /**
     * Returns the value of the counter for the current time period
     * @return The current counter value
     */
    public int getCountPerInterval(){
        if (pastPeriod(now())){
            return 0;
        }
        return count.get();

    }

    /** This method exists only to facilitate testing of this class
     * @return now (or a stubbed version of it)
     */
    public LocalDateTime now(){
        return LocalDateTime.now();
    }

    /**
     * @param now the current time
     * @return true if the current time has exceeded the current period
     */
    private boolean pastPeriod(LocalDateTime now){
        return Duration.between(lastEvent, now).toMinutes() > intervalMin;
    }
}
