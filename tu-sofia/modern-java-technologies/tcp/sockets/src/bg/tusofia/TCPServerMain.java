package bg.tusofia;

import java.io.IOException;

public class TCPServerMain {
    private static class TCPServerFactory {
        private int port;

        public TCPServerFactory(int port) {
            this.port = port;
        }

        public ITCPServer createBlockingServer() {
            return new BlockingTCPServer(port);
        }

        public ITCPServer createNonBlockingServer() {
            return new NonBlockingTCPServer(port);
        }
    }

    public static void main(String[] argv) throws IOException {
        if (argv == null || argv.length != 1) {
            System.out.println("Pass 'true' for the blocking and 'false' for the non-blocking TCP server");
            System.exit(1);
        }
        TCPServerFactory f = new TCPServerFactory(ITCPServer.DEFAULT_PORT);
        ITCPServer server = Boolean.valueOf(argv[0]) ? f.createBlockingServer() : f.createNonBlockingServer();
        server.startServer();
    }
}
