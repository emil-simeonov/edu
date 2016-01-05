package bg.tusofia;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class BlockingTCPServer implements ITCPServer {
    private int port;

    public BlockingTCPServer(int port) {
        this.port = port;
    }

    @Override
    public void startServer() throws IOException {
        ServerSocket ss = new ServerSocket(port);
        System.out.println("Blocking sockets server started.");
        while (true) {
            // Stop the server if the current thread is interrupted.
            if (Thread.currentThread().isInterrupted()) {
                ss.close();
                return;
            }
            Socket s = ss.accept(); //The thread is blocked.
            //New connection is established. Read the request and send simple response

            BufferedReader reader = null;
            PrintWriter writer = null;
            try {
                reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
                writer = new PrintWriter(s.getOutputStream());
                sendResponse(writer, obtainInput(reader));
            } finally {
                if (reader != null) {
                    reader.close();
                }
                if (writer != null) {
                    writer.close();
                }
                if (s != null) {
                    s.close();
                }
            }
        }
    }

    private void sendResponse(PrintWriter writer, String input) throws IOException {
        writer.println("Blocking sockets server... " + input);
        writer.flush();
    }

    private String obtainInput(BufferedReader reader) throws IOException {
        StringBuilder input = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) {
                // new line or any "empty" line should terminate further reading from the socket
                break;
            }
            input.append(line).append("\n");
        }
        return input.toString();
    }

    public static void main(String[] argv) throws IOException {
        new BlockingTCPServer(ITCPServer.DEFAULT_PORT).startServer();
    }
}
