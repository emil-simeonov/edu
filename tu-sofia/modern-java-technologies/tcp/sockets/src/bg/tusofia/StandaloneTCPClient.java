package bg.tusofia;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class StandaloneTCPClient {
    public static void main(String[] argv) throws IOException {
        Socket s = null;
        try {
            s = new Socket("www.tu-sofia.bg", 80);
            PrintWriter pw = new PrintWriter(s.getOutputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
            pw.println("GET /index.html");
            pw.println();
            pw.flush();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    break;
                }
                System.out.println(line);
            }
        } finally {
            if (s != null) {
                s.close();
            }
        }
    }
}
