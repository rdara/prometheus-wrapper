# prometheus-wrapper
Prometheus Metrics with a choice of labels support.
This is complete application that demos metrics support with the following technologies:
**scala, akka-http, prometheus**

The following package that support metrics should be of primary interest: 
**pers.rdara.prometheus.wrapper.metrics**

## Basic Setup from command line
>https://github.com/rdara/prometheus-wrapper.git
> 
>./gradelw run

>curl http://localhost:12345/demo/short
> 
>curl http://localhost:12345/demo/long
> 
>curl http://localhost:12345/demo/error
> 

and observe metrics with:
>curl http://localhost:12345/metrics

### Default Metrics Sample
>prometheus_wrapper_default_get_demo_short_total_messages  2.0
> 
>prometheus_wrapper_default_get_demo_short_processing_time_sum 497.0
> 
>prometheus_wrapper_default_get_demo_short_inflight_messages 0.0
> 
>prometheus_wrapper_default_get_demo_error_errored_total_messages 1.0
> 

### Labelled Metrics Sample
>prometheus_wrapper_total_messages{method="GET",resource="demo",resource_2="short",resource_3="",} 1.0
> 
>prometheus_wrapper_processing_time_count{method="GET",resource="demo",resource_2="short",resource_3="",} 1.0
> 
>prometheus_wrapper_processing_time_sum{method="GET",resource="demo",resource_2="short",resource_3="",} 523.0

## How to use
There are 2 scala singleton objects, DefaultMetrics and LabelledMetrics are available to use. You can use either or both within the same application.

### DefaultMetrics 
Decide which keys you would like to use and everything else is provisioned by this prometheus_wrapper's DefaultMetrics.

#### metrics.conf or akka.http.conf.metrics.default_keys system property
>DefaultMetrics.startMessage()
> 
>DefaultMetrics.completedMessage(getDuration(ctx.request))
> 
>DefaultMetrics.erroredMessage(getDuration(request))
> 

### Custom Keys
>DefaultMetrics.startMessage(Metrics.getMetricKeys(request))
> 
>DefaultMetrics.completedMessage(getDuration(ctx.request), Metrics.getMetricKeys(ctx.request))
> 
>DefaultMetrics.erroredMessage(getDuration(request), Metrics.getMetricKeys(request))

### LabelledMetrics
Decide which keys and labels you would like to use and everything else is provisioned by this prometheus_wrapper's DefaultMetrics.

#### metrics.conf or system properties
akka.http.conf.metrics.max_no_of_labels
akka.http.conf.metrics.label_names
akka.http.conf.metrics.labelled_keys

>LabelledMetrics.startMessage(Metrics.getMetricLables(request))
>
>LabelledMetrics.completedMessage(getDuration(ctx.request), Metrics.getMetricLables(ctx.request))
> 
>LabelledMetrics.erroredMessage(getDuration(request), Metrics.getMetricLables(request))

### Custom Keys
>LabelledMetrics.startMessage(Metrics.getLabelledMetricKeys(), Metrics.getMetricLables(request))
> 
>LabelledMetrics.completedMessage(getDuration(ctx.request), Metrics.getLabelledMetricKeys(), Metrics.getMetricLables(ctx.request))
> 
>LabelledMetrics.erroredMessage(getDuration(request), Metrics.getLabelledMetricKeys(), Metrics.getMetricLables(request))

## Configuration
The following resource file provides the configuration
>prometheus-wrapper/src/main/resources/metrics.conf

System properties like 
>**akka.http.conf.metrics.no_of_labels**
> 
prevail over the metrics.conf. System properties do take precedence. 
pers.rdara.prometheus.wrapper.metrics.LabelledMetricsTest test demonstrates system properties precedence.

## Concepts demonstrated by this prometheus_wrapper application

### Typesafe Configuration
metrcis.conf and ApplicationConfig demonstrates how to have a type safe configuration for your application

### Metrics: Dont Repeat Yourself (DRY), Completeness, Futuristic and Maintainable
You application can have several REST APIs and you would like to capture metrics. 
**DRY**: You dont want to handle the separation of concerns like "Metrics, logging,.." in every REST API handler and you would like to concentrate on pure business logic over there.
**Completeness**: You want to capture metrics for all your REST APIs
**Futuristic**: And, when a new REST API has been introduced, you would like to seamlessly support metrics.
**Maitainable**: Change should be easy and when it handled at a centralized place, it would be easy.

All the above characteristics are demonstrated at
>pers.rdara.akka.http.test.server.common.PrometheusMetricsDirectives
>pers.rdara.akka.http.test.server.TestSever
>>   initiatePrometheusMetrics(materializer)
>>
>>   completePrometheusMetrics(materializer)
> 
>pers.rdara.akka.http.test.server.common.CommonExceptionHandler 

### Less Code and More Use cases and Flexibility
This prometheus-wrapper project demonstrates the flexibility of using metrics with or without labels, mixing them however you would like to. And all this, with very less code by DRY and inheritance as demonstrated by
> pers.rdara.prometheus.wrapper.metrics.DefaultMetrics
> 
> pers.rdara.prometheus.wrapper.metrics.LabelledMetrics

The fat AbstractMetrics handles all the required nuances and constrains of prometheus library. A tiny derivered classes like DefaultMetrics and LabelledMetrics offering the user all the needed flexibility.
The "default" values eases the user to supply the same values every time while using the api.

### Gradle versions
versions_$xxxx.gradle is an idea to consolidate the "versions" of all the dependencies. This is demonstrated by
> build.gradle
>>apply from: rootProject.file("versions_scala_2.12.gradle")
>
> versions_scala_3.gradle
> 
> version_scala_2.12.gradle
> 
The backward incompatible nature of scala has challenged the scala developers and there are scala verion dependencies everywhere. This approach mitigates this.
Also, if one still wants to use the "same versions" of dependencies across the organization, then the versions file can reside as a git sub module and all the git modules could depend on that.


