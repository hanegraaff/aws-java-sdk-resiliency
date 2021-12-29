package com.hanegraaff.resiliency.builders;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.retry.PredefinedBackoffStrategies;
import com.amazonaws.retry.PredefinedRetryPolicies;
import com.amazonaws.retry.RetryPolicy;

public class ConfigurationBuilder {

    /**
     * Creates a configuration suitable for long-running batch process without
     * a strict sla. It will try each operation for up to 10 minutes
     *
     * This configuration will:
     * 1. 10 retries with 1-5 seconds between retries
     * 60 second request timeout
     *
     * @return the configured object
     */
    static public ClientConfiguration slowAndSteady(){
        RetryPolicy retryPolicy = new RetryPolicy(new PredefinedRetryPolicies.SDKDefaultRetryCondition(),
                new PredefinedBackoffStrategies.FullJitterBackoffStrategy(1000, 50000),
                10,
                false);

        ClientConfiguration config = new ClientConfiguration();
        config.setConnectionTimeout(0);
        config.setRequestTimeout(60 * 1000);
        config.setClientExecutionTimeout(0);
        config.setRetryPolicy(retryPolicy);

        return config;
    }

    /**
     * Creates a configuration suitable for a process that would prefer to fail fast but
     * still wants to retry errors. Each request will be tried for up to 50 seconds
     *
     * This configuration will:
     * 1. 5 retries with 1-3 seconds between retries
     * 10 second request timeout
     *
     * @return the configured object
     */
    static public ClientConfiguration retryAndFailFast(){
        RetryPolicy retryPolicy = new RetryPolicy(new PredefinedRetryPolicies.SDKDefaultRetryCondition(),
                new PredefinedBackoffStrategies.FullJitterBackoffStrategy(1000, 3),
                5,
                false);

        ClientConfiguration config = new ClientConfiguration();
        config.setConnectionTimeout(0);
        config.setRequestTimeout(10 * 1000);
        config.setClientExecutionTimeout(0);
        config.setRetryPolicy(retryPolicy);

        return config;
    }

    /**
     * Creates a configuration suitable for a front end where each operation will
     * tried for up to 8 seconds
     *
     * This configuration will:
     * 1. 2 retries with 250ms to 750ms delay between retries
     * 2. 4 second request timeout
     *
     * @return the configured object
     */
    static public ClientConfiguration responsiveUI(){
        RetryPolicy retryPolicy = new RetryPolicy(new PredefinedRetryPolicies.SDKDefaultRetryCondition(),
                new PredefinedBackoffStrategies.FullJitterBackoffStrategy(250, 750),
                2,
                false);

        ClientConfiguration config = new ClientConfiguration();
        config.setConnectionTimeout(0);
        config.setRequestTimeout(4 * 1000);
        config.setClientExecutionTimeout(0);
        config.setRetryPolicy(retryPolicy);

        return config;
    }
}
