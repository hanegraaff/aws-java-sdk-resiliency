package main;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.hanegraaff.resiliency.builders.ConfigurationBuilder;
import com.hanegraaff.resiliency.handlers.ServiceHealthRequestHandler;
import java.util.List;
import com.hanegraaff.resiliency.handlers.ProgrammableExceptionRequestHandler;
import com.hanegraaff.resiliency.health.ServiceHealthTracker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ClientConfigurationDemo {

    private static final Log logger = LogFactory.getLog(ClientConfigurationDemo.class);

    private static void listS3Buckets(AmazonS3 s3Client, String testName){
        logger.info("Running Test: " + testName);
        List<Bucket> buckets;
        try{
            buckets = s3Client.listBuckets();
        }
        catch (Exception e){
            logger.warn("Could not list buckets because of: " + e);
            return;
        }

        for (Bucket b : buckets) {
            logger.info("* " + b.getName());
        }
    }

    public static void main(String[] args) {
        logger.info("Starting Resilient Configuration test");

        ServiceHealthTracker s3HealthTracker = new ServiceHealthTracker();


        AmazonS3 slowAndSteady = AmazonS3ClientBuilder.standard().
                withRequestHandlers(new ServiceHealthRequestHandler(0.5, 10, s3HealthTracker), new ProgrammableExceptionRequestHandler(new AmazonS3Exception("injected error"), 0.5)).
                withClientConfiguration(ConfigurationBuilder.slowAndSteady()).
                build();

        AmazonS3 retryAndFailFast = AmazonS3ClientBuilder.standard().
                withRequestHandlers(new ServiceHealthRequestHandler(0.5, 10, s3HealthTracker)).
                withClientConfiguration(ConfigurationBuilder.retryAndFailFast()).
                build();

        AmazonS3 responsiveUI = AmazonS3ClientBuilder.standard().
                withRequestHandlers(new ServiceHealthRequestHandler(0.5, 10, s3HealthTracker)).
                withClientConfiguration(ConfigurationBuilder.responsiveUI()).
                build();


        listS3Buckets(slowAndSteady, "slowAndSteady");
        listS3Buckets(retryAndFailFast, "retryAndFailFast");
        listS3Buckets(responsiveUI, "responsiveUI");
    }
}
