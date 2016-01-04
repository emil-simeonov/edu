package bg.tusofia;

import java.io.IOException;

public interface ITCPServer {
    int DEFAULT_PORT = 9000;

    void startServer() throws IOException;
}
