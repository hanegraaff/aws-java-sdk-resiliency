package com.hanegraaff.resiliency.handlers;

import com.amazonaws.AmazonClientException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProgrammableExceptionRequestHandlerTest {

    @Test
    public void testValidInvalidParameters(){
        assertDoesNotThrow(() -> {
            new ProgrammableExceptionRequestHandler(new AmazonClientException("test"), 0.5);
        });

        assertThrows(IllegalArgumentException.class, () -> new ProgrammableExceptionRequestHandler(null, 0.5));
        assertThrows(IllegalArgumentException.class, () -> new ProgrammableExceptionRequestHandler(new AmazonClientException("test"), -1));
        assertThrows(IllegalArgumentException.class, () -> new ProgrammableExceptionRequestHandler(new AmazonClientException("test"), 1.1));
    }

    @Test
    public void testThrowsException(){

        assertDoesNotThrow(() -> {
            ProgrammableExceptionRequestHandler testHandler = new ProgrammableExceptionRequestHandler(new AmazonClientException("test"), 0);
            testHandler.beforeAttempt(null);
        });

        assertThrows(AmazonClientException.class, () -> {
            ProgrammableExceptionRequestHandler testHandler = new ProgrammableExceptionRequestHandler(new AmazonClientException("test"), 1);
            testHandler.beforeAttempt(null);
        });

    }


}