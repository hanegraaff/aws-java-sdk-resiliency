package com.hanegraaff.resiliency.health;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ServiceHealthTrackerTest {

    @Test
    public void testTransitionChange(){
        ServiceHealthTracker testTracker = new ServiceHealthTracker();

        // test that state changes are tracked appropriately.

        LocalDateTime testTime = testTracker.getLastStateChange();

        testTracker.setHealthy();
        testTracker.setHealthy();
        testTracker.setHealthy();
        testTracker.setHealthy();
        assertEquals(testTime, testTracker.getLastStateChange());
        assertEquals(testTracker.getHealthState(), ServiceHealthState.HEALTHY);


        testTracker.setUnhealthy();
        assertNotEquals(testTime, testTracker.getLastStateChange());
        assertEquals(testTracker.getHealthState(), ServiceHealthState.UNHEALTHY);

        testTime = testTracker.getLastStateChange();
        testTracker.setUnhealthy();
        testTracker.setUnhealthy();
        testTracker.setUnhealthy();
        assertEquals(testTime, testTracker.getLastStateChange());
        assertEquals(testTracker.getHealthState(), ServiceHealthState.UNHEALTHY);
    }

}