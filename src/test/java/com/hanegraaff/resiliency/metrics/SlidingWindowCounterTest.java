package com.hanegraaff.resiliency.metrics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class SlidingWindowCounterTest {

    @AfterEach
    void tearDown() {
    }

    @Test
    void testIncrementInPeriod() {
        // Test that initial count is 1
        SlidingWindowCounter c = new SlidingWindowCounter(5);
        assertEquals(c.increment(), 1);

        // add some more
        c.increment();
        assertEquals(c.increment(), 3);
    }

    @Test
    void testIncrementOutOfPeriod() {
        final LocalDateTime TEST_TIME = LocalDateTime.parse("3000-01-01T00:00:00.000");


        SlidingWindowCounter c = new SlidingWindowCounter(5);
        SlidingWindowCounter spyCounter = Mockito.spy(c);

        spyCounter.increment();
        spyCounter.increment();
        spyCounter.increment();

        when(spyCounter.now()).thenReturn(TEST_TIME);
        assertEquals(spyCounter.increment(), 1);
    }
}