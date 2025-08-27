import java.net.*;

public class Server {
    private static int port;
    private static ServerSocket serverSock;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Port number must be provided");
            return;
        }

        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception e) {
            System.out.println("Enter a valid port number");
            return;
        }

        Socket clientSock;
        Connection conn;

        try {
            serverSock = new ServerSocket(port);

            while (true) {
                clientSock = serverSock.accept();
                System.out.printf("Established connection to %s\n", clientSock.getRemoteSocketAddress());
                conn = new Connection(clientSock);
                conn.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}