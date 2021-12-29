package com.hanegraaff.resiliency.handlers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NetworkDelayRequestHandlerTest {
    @Test
    public void testValidInvalidParameters(){
        assertDoesNotThrow(() -> {
            new NetworkDelayRequestHandler(0.5, 100, 100);
        });

        assertThrows(IllegalArgumentException.class, () -> new NetworkDelayRequestHandler(-1, 100, 100));
        assertThrows(IllegalArgumentException.class, () -> new NetworkDelayRequestHandler(1.1, 100, 100));
        assertThrows(IllegalArgumentException.class, () -> new NetworkDelayRequestHandler(0.6, -1, 100));
        assertThrows(IllegalArgumentException.class, () -> new NetworkDelayRequestHandler(0.6, 100, -1));
    }
}