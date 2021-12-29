package main;

import com.amazonaws.AmazonClientException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.retry.PredefinedBackoffStrategies;
import com.amazonaws.retry.PredefinedRetryPolicies;
import com.amazonaws.retry.RetryPolicy;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.hanegraaff.resiliency.builders.ConfigurationBuilder;
import com.hanegraaff.resiliency.handlers.ProgrammableExceptionRequestHandler;
import com.hanegraaff.resiliency.handlers.ServiceHealthRequestHandler;
import com.hanegraaff.resiliency.health.ServiceHealthTracker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

public class HandlersDemo {

    private static final Log logger = LogFactory.getLog(HandlersDemo.class);

    public static void main(String[] args) {
        logger.info("Starting Resilient Configuration test");

        ServiceHealthTracker s3HealthTracker = new ServiceHealthTracker();

        //
        // Demonstrates the use of the ServiceHealthRequestHandler handler
        //
        logger.info("Demonstrating use of ServiceHealthRequestHandler");
        logger.info(String.format("Service health before the test is: %s", s3HealthTracker.getHealthState()));

        ClientConfiguration config = getFaultyConfiguration();

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().
                withRequestHandlers(new ServiceHealthRequestHandler(0.5, 5, s3HealthTracker)).
                withClientConfiguration(config).
                build();
        listS3Buckets(s3Client, "Service Health Demo");

        logger.info(String.format("Service health after the test is: %s", s3HealthTracker.getHealthState()));

        //
        // Demonstrates the use of the ProgrammableExceptionRequestHandler handler
        //
        logger.info("Demonstrating use of ProgrammableExceptionRequestHandler");

        config = ConfigurationBuilder.slowAndSteady();

        //inject an exception 99% of the times.
        s3Client = AmazonS3ClientBuilder.standard().
                withRequestHandlers(new ProgrammableExceptionRequestHandler(new AmazonClientException("test"), 0.99)).
                withClientConfiguration(config).
                build();

        listS3Buckets(s3Client, "Programmable Exception Demo");

    }


    /**
     * Returns a configuration object that is misconfigured to time out almost right away
     * @return A ClientConfiguration object suitable to demonstrate the
     * "ServiceHealthRequestHandler" handler.
     */
    private static ClientConfiguration getFaultyConfiguration(){
        RetryPolicy retryPolicy = new RetryPolicy(new PredefinedRetryPolicies.SDKDefaultRetryCondition(),
                new PredefinedBackoffStrategies.FullJitterBackoffStrategy(100, 500),
                6,
                false);

        ClientConfiguration config = new ClientConfiguration();
        config.setConnectionTimeout(0);
        config.setRequestTimeout(50); // timeout right away
        config.setClientExecutionTimeout(0);
        config.setRetryPolicy(retryPolicy);

        return config;
    }


    private static void listS3Buckets(AmazonS3 s3Client, String testName) {
        logger.info("Running Test: " + testName);
        List<Bucket> buckets;
        try {
            buckets = s3Client.listBuckets();
        } catch (Exception e) {
            logger.warn("Could not list buckets because of: " + e);
            return;
        }

        for (Bucket b : buckets) {
            logger.info("* " + b.getName());
        }
    }
}