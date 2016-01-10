package bg.tusofia.vertx;

import bg.tusofia.tcp.TCPConstants;
import io.vertx.core.Vertx;
import io.vertx.core.streams.Pump;

public class EchoNonBlockingTCPServer {
    public static void main(String[] argv) {
        Vertx.vertx().createNetServer().connectHandler(
                sock -> {
                    // Create a pump
                    Pump.pump(sock, sock).start();
                }
        ).listen(TCPConstants.DEFAULT_PORT);
    }
}
