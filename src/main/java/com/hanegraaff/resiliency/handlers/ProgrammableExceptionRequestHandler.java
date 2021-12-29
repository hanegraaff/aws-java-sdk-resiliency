package com.hanegraaff.resiliency.handlers;

import com.amazonaws.AmazonClientException;
import com.amazonaws.handlers.HandlerBeforeAttemptContext;
import com.amazonaws.handlers.RequestHandler2;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An implementation of the RequestHandler2 class that throws a user supplied AmazonClientException
 * at random times, defined by the caller
 */
public class ProgrammableExceptionRequestHandler extends RequestHandler2 {

    private final Log log = LogFactory.getLog(ProgrammableExceptionRequestHandler.class);
    private final double errorRate;
    private final AmazonClientException customException;


    /**
     * Constructs a new handler with
     * 1. A 5% error rate
     * 2. simple AmazonClientException exception
     */
    public ProgrammableExceptionRequestHandler(){
        this.errorRate = 0.05;
        this.customException = new AmazonClientException("Injected Error");
    }

    /**
     * Constructs a new handler given a custom exception and error rate value
     * @param customException The user supplied AmazonClientException or subclass of it
     * @param errorRate The per
     */
    public ProgrammableExceptionRequestHandler(AmazonClientException customException, double errorRate){

        if (customException == null)
            throw new IllegalArgumentException("Custom exception parameter cannot be be null.");

        if (errorRate < 0 || errorRate > 1)
            throw new IllegalArgumentException("Error rate parameter is out of range. Must be between 0 and 1");

        this.customException = customException;
        this.errorRate = errorRate;
    }

    /**
     * Overrides the afterAttempt method to periodically raise a custom
     * AmazonClientException. Note that this will bypass all internal SDK
     * retry mechanisms.
     *
     * @param context the SDK supplied HandlerAfterAttemptContext object
     */
    public void beforeAttempt(HandlerBeforeAttemptContext context){
        if (Math.random() < errorRate){
            log.info("Injecting custom error: " + this.customException);
            throw customException;
        }
    }
}
