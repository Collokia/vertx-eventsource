package org.collokia.vertx.eventsource

import io.vertx.core.AbstractVerticle
import io.vertx.core.json.JsonObject
import org.glassfish.jersey.media.sse.EventSource
import org.glassfish.jersey.media.sse.SseFeature
import javax.ws.rs.client.ClientBuilder
import kotlin.properties.Delegates

class EventSourceConsumerVerticle : AbstractVerticle() {

    var eventSource: EventSource by Delegates.notNull()

    override fun start() {
        val client = ClientBuilder.newBuilder().register(javaClass<SseFeature>()).build()

        val encoding    = config().getString("eventConsumer.encoding") ?: "UTF-8"
        val endpointURI = config().getString("eventConsumer.endpointURI")
        val address     = config().getString("eventConsumer.address")

        assert(endpointURI != null, "'eventConsumer.endpointURI' can't be empty")
        assert(address != null,  "'eventConsumer.address' can't be empty")

        val webTarget = client.target(endpointURI)
        eventSource = EventSource(webTarget)
        eventSource.register { event ->
            val dataJson = JsonObject(String(event.getRawData(), encoding))
            vertx.eventBus().send(address, dataJson)
        }
        eventSource.open()
    }

    override fun stop() {
        eventSource.close()
    }

}