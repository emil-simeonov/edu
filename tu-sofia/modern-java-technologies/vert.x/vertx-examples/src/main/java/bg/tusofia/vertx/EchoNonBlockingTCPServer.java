package bg.tusofia.vertx;

import bg.tusofia.tcp.TCPConstants;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.streams.Pump;

// We extend the abstract verticle, so we could add this service to a message bus, if necessary.
public class EchoNonBlockingTCPServer extends AbstractVerticle {
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

    public static void main(String[] argv) throws Exception {
        // Vert.x deployes and uses "verticals". Verticals are event-driven.
        // New verticals could be deployed at any time - as dynamic as necessary
        Vertx.vertx().deployVerticle(new EchoNonBlockingTCPServer());
    }
}
