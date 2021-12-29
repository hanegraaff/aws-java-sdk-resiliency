package com.hanegraaff.resiliency.handlers;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.Request;
import com.amazonaws.handlers.HandlerAfterAttemptContext;
import com.hanegraaff.resiliency.health.ServiceHealthState;
import com.hanegraaff.resiliency.health.ServiceHealthTracker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServiceHealthRequestHandlerTest {

    HandlerAfterAttemptContext mockContext;

    @BeforeEach
    void setUp() {
        this.mockContext = Mockito.mock(HandlerAfterAttemptContext.class);
        Request mockRequest = Mockito.mock(Request.class);
        URI mockUri = Mockito.mock(URI.class);
        AmazonWebServiceRequest mockOriginalRequest = Mockito.mock(AmazonWebServiceRequest.class);
        Mockito.when(mockContext.getRequest()).thenReturn(mockRequest);
        Mockito.when(mockRequest.getEndpoint()).thenReturn(mockUri);
        Mockito.when(mockRequest.getOriginalRequest()).thenReturn(mockOriginalRequest);
    }

    @Test
    public void testServiceAppearsHealthy(){
        ServiceHealthTracker testTracker = new ServiceHealthTracker();
        ServiceHealthRequestHandler testHandler = new ServiceHealthRequestHandler(0, 5, testTracker);

        Mockito.when(mockContext.getException()).thenReturn(null);

        testHandler.afterAttempt(this.mockContext);
        testHandler.afterAttempt(this.mockContext);
        testHandler.afterAttempt(this.mockContext);
        testHandler.afterAttempt(this.mockContext);
        testHandler.afterAttempt(this.mockContext);

        assertEquals(testTracker.getHealthState(), ServiceHealthState.HEALTHY);
    }


    @Test
    public void testServiceAppearsUnHealthy(){
        ServiceHealthTracker testTracker = new ServiceHealthTracker();
        ServiceHealthRequestHandler testHandler = new ServiceHealthRequestHandler(0, 1, testTracker);

        Exception e = new Exception("test exception");
        Mockito.when(mockContext.getException()).thenReturn(e);

        testHandler.afterAttempt(this.mockContext);
        testHandler.afterAttempt(this.mockContext);
        testHandler.afterAttempt(this.mockContext);
        testHandler.afterAttempt(this.mockContext);
        testHandler.afterAttempt(this.mockContext);

        assertEquals(testTracker.getHealthState(), ServiceHealthState.UNHEALTHY);
    }
}