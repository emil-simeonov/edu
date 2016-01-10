package bg.tusofia.vertx;

import bg.tusofia.tcp.TCPConstants;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.streams.Pump;

/**
 * This class is a very simple non-blocking TCP echo server (it sends back all of the input AS IS to any given client).
 * The general idea of this example is to demonstrate how we could quickly implement any server as Verticals managed by
 * the vert.x platform.
 * <p>
 * Verticals are the building block of vert.x-based applications. They could be dynamically deployed at any point of
 * time. They could also interact with other verticals via the vert.x Message Bus. Verticals are reactive in the sense
 * that they are active only when processing a vert.x event. For example, the incoming TCP connection on port
 * TCPConstants.DEFAULT_PORT triggers a vert.x event, which is then handled and processed by the anonymous connect
 * handler below. Last but not least, each Vertical is guaranteed to always run in one and the same thread. This is all
 * managed by vert.x.
 */
public class NonBlockingEchoTCPServer extends AbstractVerticle {
    public static void main(String[] argv) throws Exception {
        // Vert.x deployes and uses "verticals". Verticals are event-driven.
        // New verticals could be deployed at any time - as dynamic as necessary
        Vertx.vertx().deployVerticle(new NonBlockingEchoTCPServer());
    }

    @Override
    public void start() throws Exception {
        // Creates a non-blocking TCP server which registers a handler for incoming connections (anonymous Java function)
        vertx.createNetServer().connectHandler(
                sock -> {
                    System.out.println("Processing incoming connection. Pumping out everything...");
                    // Create and start a pump. Instances of this class read items from a {@link ReadStream} and write them to a {@link WriteStream}.
                    Pump.pump(sock, sock).start();
                }
        ).listen(TCPConstants.DEFAULT_PORT); // binds the server to a given server socket (channel)
        System.out.println("Simple Echo non-blocking TCP server is started.");
    }
}
