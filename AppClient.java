import java.net.*;
import java.io.*;

/**
 * @class AppClient
 *
 * Main point of client app.
 */
public class AppClient {

    public static void main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            System.out.println("USAGE:");
            System.out.println("AppClient.java <ip> [<file name in server> [<file name to save>]]");
            return;
        }

        String address = args[0];
        int serverPort = 1234;

        System.out.println("Connecting to " + address + ":" + serverPort + "...");

        Socket socket;
        try {
            socket = new Socket(InetAddress.getByName(address), serverPort);
            System.out.println("Connected.\n");

            try {
                new ClientInstance(socket, args).process();
            } finally {
                socket.close();
            }
        } catch (IOException ex) {
            System.out.println("Unable to connect to " + address + ":" + serverPort + ".");
        }
    }
}
