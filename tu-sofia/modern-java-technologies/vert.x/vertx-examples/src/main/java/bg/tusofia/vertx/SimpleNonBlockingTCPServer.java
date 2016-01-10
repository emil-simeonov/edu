package bg.tusofia.vertx;

import bg.tusofia.tcp.TCPConstants;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;

/**
 * This class is a very simple non-blocking TCP server implemented as a Vert.x Vertical.
 * It makes it pretty clear how incoming connections are handled. Socket reading and writing is also illustrated.
 * This example could easily evolve into a complex TCP service.
 */
public class SimpleNonBlockingTCPServer extends AbstractVerticle {
    public static void main(String[] argv) {
        Vertx.vertx().deployVerticle(new SimpleNonBlockingTCPServer());
    }

    @Override
    public void start() throws Exception {
        vertx.createNetServer().connectHandler(sock -> {
            // In order to be able to read and write to the socket when new connections are established we register
            // our own handler for incoming transports.
            sock.handler(in -> {
                // First we read from the socket
                String msg = in.getString(0, in.length());
                // We create a vert.x buffer where we will write our response message
                Buffer out = Buffer.buffer();
                out.appendString("You sent me this message: ").appendString(msg);
                // Then we write the response message to the socket
                sock.write(out);
            });
        }).listen(TCPConstants.DEFAULT_PORT);
        System.out.println("Simple non-blocking TCP server is started.");
    }
}
