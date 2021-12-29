package com.hanegraaff.resiliency.handlers;

import com.amazonaws.handlers.HandlerBeforeAttemptContext;
import com.amazonaws.handlers.RequestHandler2;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.ThreadLocalRandom;

/**
 * An implementation of the RequestHandler2 class simulates a network delay
 * by forcing the thread to sleep for random amount of time within a boundary
 *
 */
public class NetworkDelayRequestHandler extends RequestHandler2 {

    private final Log log = LogFactory.getLog(NetworkDelayRequestHandler.class);
    private final double delayRate;

    private final int baseDelayMs;
    private final int offsetDelayMs;

    /**
     * Constructs a new handler with
     * 1. A 5% delay rate
     * 2. A delay between 1-3 seconds
     */
    public NetworkDelayRequestHandler(){
        this.delayRate = 0.05;
        this.baseDelayMs = 1000;
        this.offsetDelayMs = 2000;
    }

    /**
     * Constructs a new handler based on user supplied parameters.
     * The actual delay is an amount between baseDelayMs and (baseDelayMs + offsetDelayMs)
     *
     * @param delayRate The percentage (between 0 an 1) of times a delay is injected.
     * @param baseDelayMs The minimum delay in milliseconds
     * @param offsetDelayMs The delay offset, also in milliseconds
     */
    public NetworkDelayRequestHandler(double delayRate, int baseDelayMs, int offsetDelayMs){
        if (delayRate < 0 || delayRate > 1) {
            throw new IllegalArgumentException("'delayRate' parameter is out of range. Must be between 0 and 1");
        }

        if (baseDelayMs <= 0 || offsetDelayMs < 0) {
            throw new IllegalArgumentException("'baseDelayMs' and 'offsetDelayMs' must be positive integers");
        }

        this.delayRate = delayRate;
        this.baseDelayMs = baseDelayMs;
        this.offsetDelayMs = offsetDelayMs;
    }

    /**
     * Overrides the afterAttempt method to periodically raise a custom
     * AmazonClientException. Note that this will bypass all internal SDK
     * retry mechanisms.
     *
     * @param context the SDK supplied HandlerAfterAttemptContext object
     */
    public void beforeAttempt(HandlerBeforeAttemptContext context){

        if (Math.random() < delayRate){
            int delayMs = ThreadLocalRandom.current().nextInt(baseDelayMs, (baseDelayMs +  offsetDelayMs) + 1);

            log.info(String.format("Injecting custom delay of %d milliseconds", delayMs));
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                log.warn("Delay failed because of an exception: " + e.getCause());
            }
        }
    }
}
