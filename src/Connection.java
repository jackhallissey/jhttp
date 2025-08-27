import java.net.*;
import java.io.*;

class Connection extends Thread {
    private Socket sock;

    public Connection(Socket s) {
        sock = s;
    }

    @Override
    public void run() {
        try (InputStream in = sock.getInputStream();
            InputStreamReader reader = new InputStreamReader(in);
            BufferedReader bfReader = new BufferedReader(reader);
            OutputStream out = sock.getOutputStream();
            BufferedOutputStream bfOut = new BufferedOutputStream(out)) {
            
            handleRequest(bfReader, bfOut);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the socket even if an exception was thrown
            close();
        }
    }

    public void handleRequest(BufferedReader bf, BufferedOutputStream out) throws IOException {
            Response r;

            try {
                String[] request = new String[50];

                for (int i = 0; i < request.length; i++) {
                    String line = bf.readLine();
                    if (line == null || line.isEmpty()) {
                        break;
                    }
                    request[i] = line;
                }

                r = new Response(request);
            } catch (Exception e) {
                e.printStackTrace();
                // If an error occurs, respond with 500 Internal Server Error
                r = new Response(500);
            }

            r.send(out);
    }

    public void close() {
        try {
            sock.close();
            System.out.printf("Closed connected to %s\n", sock.getRemoteSocketAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}