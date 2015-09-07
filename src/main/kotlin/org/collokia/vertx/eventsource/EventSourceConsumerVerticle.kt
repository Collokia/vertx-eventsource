package org.collokia.vertx.eventsource

import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import nl.komponents.kovenant.async
import org.glassfish.jersey.media.sse.EventSource
import org.glassfish.jersey.media.sse.SseFeature
import javax.ws.rs.client.ClientBuilder
import kotlin.properties.Delegates

class EventSourceConsumerVerticle : AbstractVerticle() {

    companion object {
        val log = LoggerFactory.getLogger("EventSourceConsumerVerticle")
    }

    var eventSource: EventSource by Delegates.notNull()

    override fun start(startFuture: Future<Void>) {
        async {
            val encoding    = config().getString("eventConsumer.encoding") ?: "UTF-8"
            val endpointURI = config().getString("eventConsumer.endpointURI")
            val address     = config().getString("eventConsumer.address")

            log.info("Trying to start EventSource consumer of $endpointURI")

            val client = ClientBuilder.newBuilder().register(javaClass<SseFeature>()).build()

            assert(endpointURI != null, "'eventConsumer.endpointURI' can't be empty")
            assert(address != null, "'eventConsumer.address' can't be empty")

            val webTarget = client.target(endpointURI)
            eventSource = EventSource(webTarget)
            eventSource.register { event ->
                val dataJson = JsonObject(String(event.getRawData(), encoding))
                vertx.eventBus().send(address, dataJson)
            }

            log.info("Started EventSource consumer of $endpointURI")
            startFuture.complete()

            eventSource.open()
        }
    }

    override fun stop() {
        eventSource.close()
    }

}