import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;

class Response {
    private static String headersTemplate = "HTTP/1.1 %d %s\r\nContent-Type: text/html; charset=utf-8\r\n\r\n";
    private static String errorBodyTemplate = "<!DOCTYPE html><html lang=\"en\"><head><title>Error</title></head><body><h1>%d %s</h1></body></html>";
    private static byte[] responseTerminator = "\r\n\r\n".getBytes();

    private static HashMap<Integer, String> statusText = initMap();
    
    private static HashMap<Integer, String> initMap() {
        HashMap<Integer, String> map = new HashMap<>();
        map.put(200, "OK");
        map.put(400, "Bad Request");
        map.put(404, "Not Found");
        map.put(405, "Method Not Allowed");
        map.put(500, "Internal Server Error");
        map.put(505, "Version Not Supported");
        return map;
    }

    private int status;
    private File file;      // Can be null for error responses

    public Response(String[] request) {
        if (request[0] == null)  {
            // If request line is null - respond with 400 Bad Request
            status = 400;
        } else {

            String[] requestLine = request[0].split(" ");

            if (requestLine.length != 3) {
                // If request line does not have method, path and version - respond with 400 Bad Request
                status = 400;
            } else if (!requestLine[0].equals("GET")) {
                // If method is not GET - respond with 405 Method Not Allowed
                status = 405;
            } else if (!requestLine[2].equals("HTTP/1.1")) {
                // If HTTP version is not 1.1 - respond with 505 Version Not Supported
                status = 505;
            } else {
                status = 200;

                String filepath = requestLine[1];

                if (filepath.equals("/")) {
                    filepath = "/index.html";
                }

                file = new File("./www" + filepath);
            }

        }
    }

    // For server error responses
    public Response(int s) {
        if (s < 500) {
            throw new IllegalArgumentException("For responses other than server errors, pass the request to the Response constructor");
        }
        if (!statusText.containsKey(s)) {
            throw new IllegalArgumentException("Unrecognised status");
        }
        status = s;
    }

    public void send(BufferedOutputStream bfOut) throws IOException {
        byte[] headers;
        byte[] body;

        if (status < 400 && !file.exists()) {
            status = 404;
        }

        if (status < 400) {

            try {
                // TODO - handle large files
                body = Files.readAllBytes(file.toPath());
            } catch (Exception e) {
                e.printStackTrace();
                // If an exception was thrown when reading the file, respond with 500 Internal Server Error
                status = 500;
                body = String.format(errorBodyTemplate, status, statusText.get(status)).getBytes();
            }

        } else {
            body = String.format(errorBodyTemplate, status, statusText.get(status)).getBytes();
        }

        headers = String.format(headersTemplate, status, statusText.get(status)).getBytes();

        bfOut.write(headers);
        bfOut.write(body);
        bfOut.write(responseTerminator);
        bfOut.flush();
        System.out.printf("Responded with status %d %s\n", status, statusText.get(status));
    }
}
