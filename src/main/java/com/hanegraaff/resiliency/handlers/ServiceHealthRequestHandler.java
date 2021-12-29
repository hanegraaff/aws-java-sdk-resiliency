package com.hanegraaff.resiliency.handlers;

import com.amazonaws.handlers.HandlerAfterAttemptContext;
import com.amazonaws.handlers.RequestHandler2;
import com.hanegraaff.resiliency.health.ServiceHealthTracker;
import com.hanegraaff.resiliency.metrics.SlidingWindowCounter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An implementation of the RequestHandler2 class that tracks the health of AWS by
 * monitoring internal SDK errors. If the errors exceed a certain threshold over any
 * 5-minute window, the service will be considered unhealthy.
 *
 * Additionally, all failed SDK attempts will bt logged like this via Apache Common Logging:
 *
 * Error attempting Amazon S3 ListBucketsRequest operation using URL: s3.us-west-2.amazonaws.com. Caused by: HttpRequestTimeoutException
 */
public class ServiceHealthRequestHandler extends RequestHandler2 {

    private final Log log = LogFactory.getLog(ServiceHealthRequestHandler.class);

    private final SlidingWindowCounter eventCount;
    private final SlidingWindowCounter errorCount;

    private final double errorRateThreshold;
    private final int minRequests;
    private final ServiceHealthTracker serviceHealthTracker;

    final int metricInterval = 5;

    /**
     * Configures a new ServiceHealthRequestHandler object with the supplied parameters.
     *
     * @param errorRateThreshold The percentage (between 0-1) or failed requests before ServiceHealthTracker reports service as unhealthy.
     * @param minRequests The minimum number of requests/retries to consider before evaluating the service health
     * @param serviceHealthTracker the ServiceHealthTracker that encapsulates the health of the service used by the SDK client
     */
    public ServiceHealthRequestHandler(double errorRateThreshold, int minRequests, ServiceHealthTracker serviceHealthTracker) {
        this.eventCount = new SlidingWindowCounter(metricInterval);
        this.errorCount = new SlidingWindowCounter(metricInterval);
        this.errorRateThreshold = errorRateThreshold;
        this.minRequests = minRequests;
        this.serviceHealthTracker = serviceHealthTracker;
    }

    /**
     * Overrides the afterAttempt method.
     * When an error is detected, it will be tracked, and if too many of them
     * are seen over a period of time, the service will be considered unhealthy.
     *
     * @param context the SDK supplied HandlerAfterAttemptContext object
     */
    public void afterAttempt(HandlerAfterAttemptContext context) {
        Exception ex = context.getException();
        int evtCnt = eventCount.increment();
        int errCnt = errorCount.getCountPerInterval();

        String serviceName = context.getRequest().getServiceName();
        String requestURL = context.getRequest().getEndpoint().getHost();

        if (ex != null) {
            errCnt = errorCount.increment();

            String exceptionName = ex.getClass().getSimpleName();
            String operationName = context.getRequest().getOriginalRequest().getClass().getSimpleName();

            String errorMessage = String.format("Error attempting %s %s operation using URL: %s. Caused by: %s",
                    serviceName, operationName, requestURL, exceptionName);

            log.warn(errorMessage);
        }

        double errorRate = (double) errCnt / (double) evtCnt;

        if ((evtCnt > minRequests) && (errorRate > errorRateThreshold)) {
            log.warn(String.format(
                    "Excessive errors detected from %s: current error rate: %.2f, error rate threshold: %.2f. Service is considered unhealthy",
                    serviceName, errorRate, errorRateThreshold));
            serviceHealthTracker.setUnhealthy();
        } else {
            serviceHealthTracker.setHealthy();
        }
    }
}