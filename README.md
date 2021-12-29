# AWS Resiliency for Java SDK
This library augments the AWS Java SDK by providing the following capabilities.

1. Provides pre-configured `ClientConfiguration` objects tailored to different use cases.

2. Provides special Request Handlers (`RequestHandler2` derived classes) that can be used to evaluate the health of an AWS Service (for production code) or inject errors and delays (for test code).

The code supplied in this project is inteded to explore ideas around resiliency and testing of the AWS SDK. Initial development is completed, but this library is not currently available as a public maven artifact.


# How to use this library

## Creating Custom Configurations
To create custom configurations use the static `com.hanegraaff.resiliency.builders.ConfigurationBuilder` class.

```Java
/**
 * Creates a configuration suitable for long-running batch process without
 * a strict sla. It will try each operation for up to 10 minutes
 *
 * This configuration will:
 * 1. 10 retries with 1-5 seconds between retries
 * 60 second request timeout
 */

AmazonS3 slowAndSteady = AmazonS3ClientBuilder.standard().
        withClientConfiguration(ConfigurationBuilder.slowAndSteady()).
        build();

// Now thw SDK will retry errors accordingly
List<Bucket> buckets = buckets = s3Client.listBuckets();
```

## Observing the health of the AWS service used by the SDK Client
It is possible to inject a custom request handler into a Builder object that will track interal SDK Errors and determine whether the underlining service is healthy or not. 

These handlers are available in the `com.hanegraaff.resiliency.handlers` package.

Here is an example:

```Java

// The ServiceHealthTracker class can be used to track and query the percieved
// health of the service when used along the ServiceHealthRequestHandler.
ServiceHealthTracker s3HealthTracker = new ServiceHealthTracker();

logger.info(String.format("Service health before is: %s", s3HealthTracker.getHealthState()));

// Inject a ServiceHealthRequestHandler that will consider the underlining service
// unhealthy when:
// 1. At least 5 SDK operations (including retries) were made in the last 5 minutes
// 2. 50% of them resulted in an error.
AmazonS3 s3Client = AmazonS3ClientBuilder.standard().
                withRequestHandlers(new ServiceHealthRequestHandler(0.5, 5, s3HealthTracker)).
                withClientConfiguration(ConfigurationBuilder.slowAndSteady()).
                build();

List<Bucket> buckets = s3Client.listBuckets();

logger.info(String.format("Service health after is: %s", s3HealthTracker.getHealthState()));

```

When there are enough service errors, the output will look like this:

```
2021-12-29 15:57:23 INFO  HandlersDemo - Service health before is: HEALTHY
2021-12-29 15:57:24 WARN  ServiceHealthRequestHandler - Error attempting Amazon S3 ListBucketsRequest operation using URL: s3.amazonaws.com. Caused by: HttpRequestTimeoutException
2021-12-29 15:57:24 WARN  ServiceHealthRequestHandler - Error attempting Amazon S3 ListBucketsRequest operation using URL: s3.amazonaws.com. Caused by: HttpRequestTimeoutException
2021-12-29 15:57:26 WARN  ServiceHealthRequestHandler - Error attempting Amazon S3 ListBucketsRequest operation using URL: s3.amazonaws.com. Caused by: HttpRequestTimeoutException

...

2021-12-29 15:57:25 WARN  ServiceHealthRequestHandler - Excessive errors detected from Amazon S3: current error rate: 1.00, error rate threshold: 0.50. Service is considered unhealthy

...

2021-12-29 15:57:26 INFO  HandlersDemo - Service health after is: UNHEALTHY
```

## Injecting errors and delays
It is possible to inject custom exceptions and delays into the a Builder object using the custom request handlers supplied here. Please note that because these are synthetic errors and delays, they will not work in conjuction with the ClientConfiguration object supplied to a builder. This means that if Request Timeouts are set to 500ms, and a delay of 1 second is injected into each SDK operation, they will neither timeout nor internally retry. These errors and delays are useful to test how the application will handle a temporary loss of service or network congestion.

These handlers are available in the `com.hanegraaff.resiliency.handlers` package.

Here is an example:

```Java

//
// The ProgrammableExceptionRequestHandler will inject a custom Exception 
// given a rate. The following snippet will raise an AmazonClientException 
// 5% (0.05) of the times.
//
AmazonS3 s3ClientWithExceptions = AmazonS3ClientBuilder.standard().
                withRequestHandlers(new ProgrammableExceptionRequestHandler(new AmazonClientException("test"), 0.05)).
                withClientConfiguration(ConfigurationBuilder.slowAndSteady()).
                build();

List<Bucket> buckets = s3ClientWithExceptions.listBuckets();


//
// The NetworkDelayRequestHandler will inject a random delay given a rate and range.
// The following snippet will inject a delay 10% (0.1) of the times
// and last berween 1 - 4 seconds
//
AmazonS3 s3ClientWithExceptions = AmazonS3ClientBuilder.standard().
                withRequestHandlers(new NetworkDelayRequestHandler(0.1, 1000, 3000)).
                withClientConfiguration(ConfigurationBuilder.slowAndSteady()).
                build();

buckets = s3ClientWithExceptions.listBuckets();
```