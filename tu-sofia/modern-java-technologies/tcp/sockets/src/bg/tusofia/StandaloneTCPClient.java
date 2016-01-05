package bg.tusofia;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.Buffer;

public class StandaloneTCPClient {
    public static void main(String[] argv) throws IOException {
        Socket s = null;
        PrintWriter pw = null;
        BufferedReader reader = null;
        try {
            s = new Socket("www.tu-sofia.bg", 80);
            pw = new PrintWriter(s.getOutputStream());
            sendRequest(pw);
            reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
            processResponse(reader);
        } finally {
            if (pw != null) {
                pw.close();
            }
            if (reader != null) {
                reader.close();
            }
            if (s != null) {
                s.close();
            }
        }
    }

    private static void processResponse(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) {
                break;
            }
            System.out.println(line);
        }
    }

    private static void sendRequest(PrintWriter pw) {
        pw.println("GET /index.html");
        pw.println();
        pw.flush();
    }
}
