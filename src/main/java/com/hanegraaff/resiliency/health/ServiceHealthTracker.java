package com.hanegraaff.resiliency.health;

import java.time.LocalDateTime;

/**
 * A class that represents the health of an AWS Service or a specific AWS Client object.
 * When supplied to an instance of the ServiceHealthRequestHandler, the handler
 * will update the state based on how many SDK errors it detects.
 */
public class ServiceHealthTracker {

    private ServiceHealthState healthState;
    private LocalDateTime lastStateChange;

    /**
     * Creates a new ServiceHealthTracker and sets it to HEALTHY
     */
    public ServiceHealthTracker(){
        healthState = ServiceHealthState.HEALTHY;
        lastStateChange = LocalDateTime.now();
    }

    /**
     * Sets the current state to UNHEALTHY
     */
    public void setUnhealthy(){
        if (healthState == ServiceHealthState.UNHEALTHY) return;

        healthState = ServiceHealthState.UNHEALTHY;
        lastStateChange = LocalDateTime.now();
    }

    /**
     * Sets the current state to HEALTHY
     */
    public void setHealthy(){
        if (healthState == ServiceHealthState.HEALTHY) return;

        healthState = ServiceHealthState.HEALTHY;
        lastStateChange = LocalDateTime.now();
    }

    /**
     * Gets the current service health state
     * @return the current health state
     */
    public ServiceHealthState getHealthState() {
        return healthState;
    }

    /**
     * Gets the time when the current health state was set
     * @return the time since the current state
     */
    public LocalDateTime getLastStateChange(){
        return lastStateChange;
    }
}
