package bg.tusofia.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClient {

    public String sendInput() throws IOException {
        Socket s = null;
        PrintWriter writer = null;
        BufferedReader reader = null;
        try {
            s = new Socket(InetAddress.getLocalHost(), TCPConstants.DEFAULT_PORT);
            writer = new PrintWriter(s.getOutputStream());
            sendRequest(writer);
            reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
            return processResponse(reader);
        } finally {
            if (writer != null) {
                writer.close();
            }
            if (reader != null) {
                reader.close();
            }
            if (s != null) {
                s.close();
            }
        }
    }

    private String processResponse(BufferedReader reader) throws IOException {
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) {
                // new line or any "empty" line should terminate further reading from the socket
                break;
            }
            response.append(line.trim());
        }
        return response.toString();
    }

    private void sendRequest(PrintWriter writer) {
        writer.println("Hello world!");
        // We use a new line to terminate the message
        writer.println();
        writer.flush();
    }

    public static void main(String[] argv) throws IOException {
        System.out.println(new TCPClient().sendInput());
    }
}
