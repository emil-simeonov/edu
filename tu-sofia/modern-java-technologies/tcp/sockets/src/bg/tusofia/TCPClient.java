package bg.tusofia;

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
            s = new Socket(InetAddress.getLocalHost(), ITCPServer.DEFAULT_PORT);
            writer = new PrintWriter(s.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
            writer.println("Hello world!");
            writer.println();
            writer.flush();
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

    public static void main(String[] argv) throws IOException {
        System.out.println(new TCPClient().sendInput());
    }
}
