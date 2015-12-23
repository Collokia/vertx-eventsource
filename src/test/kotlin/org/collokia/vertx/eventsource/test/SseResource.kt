package org.collokia.vertx.eventsource.test

import nl.komponents.kovenant.task
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import org.glassfish.jersey.media.sse.EventOutput
import org.glassfish.jersey.media.sse.OutboundEvent
import org.glassfish.jersey.media.sse.SseFeature
import org.glassfish.jersey.servlet.ServletContainer
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces

@Path("events")
class SseResource {

    @GET
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    fun getServerSendEvents(): EventOutput {
        val eventOutput = EventOutput()

        task {
            try {
                eventOutput.write(OutboundEvent.Builder()
                    .name("WrongEventType")
                    .data(String::class.java, """{ "data": "Wrong Message!" }""")
                    .build())

                eventOutput.write(OutboundEvent.Builder()
                    .name("MyEventType")
                    .data(String::class.java, """{ "data": "Hello World!" }""")
                    .build())
            } finally {
                eventOutput.close()
            }
        }

        return eventOutput
    }

}