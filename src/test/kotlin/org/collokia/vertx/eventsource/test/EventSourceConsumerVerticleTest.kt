package org.collokia.vertx.eventsource.test

import io.vertx.core.DeploymentOptions
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import kotlin.platform.platformStatic

@RunWith(VertxUnitRunner::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class EventSourceConsumerVerticleTest {

    companion object {
        val EventsSourceHost = "localhost"
        val EventsSourcePort = 8123
        val EventType = "MyEventType"
        val Address   = "eventsource.test.address"

        val vertx       = Vertx.vertx()
        val jettyServer = Server(EventsSourcePort)

        val config = JsonObject()
            .put("eventConsumer.address", Address)
            .put("eventConsumer.eventTypes", JsonArray(listOf(EventType)))
            .put("eventConsumer.endpointURI", "http://$EventsSourceHost:$EventsSourcePort/events")

        @BeforeClass
        @platformStatic
        fun before(testContext: TestContext) {
            val context = ServletContextHandler(ServletContextHandler.SESSIONS)
            context.setContextPath("/")

            jettyServer.setHandler(context)

            val jerseyServlet = context.addServlet(javaClass<org.glassfish.jersey.servlet.ServletContainer>(), "/*")
            jerseyServlet.setInitOrder(0)
            jerseyServlet.setInitParameter("jersey.config.server.provider.classnames", javaClass<SseResource>().getCanonicalName())

            jettyServer.start()
        }
    }

    @Test
    fun testConsume(context: TestContext) {
        val messageAwaitAsync = context.async()

        // Let's start with listening to the address configured first, because just as we deploy the consumer verticle,
        // we will receive the message
        val consumer = vertx.eventBus().consumer(Address, Handler { message: Message<JsonObject> ->
            context.assertEquals("Hello World!", message.body().getString("data"))
            messageAwaitAsync.complete()
        })
        consumer.completionHandler {
            vertx.deployVerticle("org.collokia.vertx.eventsource.EventSourceConsumerVerticle", DeploymentOptions().setConfig(config), context.asyncAssertSuccess())
            // The verticle is deployed, now we either fail with timeout from messageAwaitAsync, ot it's get completed on message receive
        }
    }

}