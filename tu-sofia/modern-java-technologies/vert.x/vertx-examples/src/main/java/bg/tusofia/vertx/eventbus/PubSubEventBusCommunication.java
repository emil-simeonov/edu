package bg.tusofia.vertx.eventbus;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

/**
 * This class is an example of using the vert.x event bus benefiting out of the built-in pub/sub eventing support.
 * <p>
 * This example defines a publisher that constantly emits messages/events to any subscribed event handler, i.e.
 * "subscriber". We demonstrate the essence of pub/sub by registering a couple of subscribers as event handlers. Each
 * received event is then dumped in the console.
 */
public class PubSubEventBusCommunication {
    public static void main(String[] argv) throws InterruptedException {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new Subscriber());
        vertx.deployVerticle(new Subscriber());
        vertx.deployVerticle(new Publisher());
    }

    /**
     * A class that publishes events periodically via the Event Bus.
     */
    public static class Publisher extends AbstractVerticle {
        @Override
        public void start() throws Exception {
            // The event bus is integrated in the platform and could be accessed in this way by any vertical.
            EventBus eb = vertx.eventBus();
            // Publishes a new event every 2 sec. Events will be handled by any subscriber registered to this topic.
            vertx.setPeriodic(2000, lv -> eb.publish(EventBusConstants.TOPIC_NAME, "I am sending some news."));
        }
    }

    /**
     * A class that subscribes for a given topic (event source) and processes events when delivered by the Event Bus.
     */
    public static class Subscriber extends AbstractVerticle {
        @Override
        public void start() throws Exception {
            EventBus eb = vertx.eventBus();
            // We subscribe an event handler (anonymous Java function) for this topic
            eb.consumer(EventBusConstants.TOPIC_NAME, msg -> {
                // The event is passed
                System.out.println(String.format("%d received news: \"%s\"", hashCode(), msg.body()));
            });
        }
    }
}
