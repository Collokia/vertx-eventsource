# EventSource Client for Vertx

This [EventSource](https://developer.mozilla.org/en-US/docs/Web/API/EventSource) client allows routing the events from EventSource stream to the Vertx event-bus using the EventSource consumer verticle

## EventSource Consumer Verticle

EventSource stream consumer verticle is configured with an endpoint URI, event-bus address to route events to, and an optional array of event types to listen to:

```
JsonObject config = new JsonObject()
    .put("eventConsumer.address", "eventsource.somewhere")
    .put("eventConsumer.endpointURI", "http://somewhere.com/events")
    .put("eventConsumer.eventTypes", new JsonArray(Arrays.asList("someEvent1", "someEvent2")));
    
vertx.deployVerticle("org.collokia.vertx.eventsource.EventSourceConsumerVerticle", new DeploymentOptions().setConfig(config));
```
