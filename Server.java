import java.net.*;
import java.io.*;

/**
 * Server app.
 */
public class Server {

    public static void main(String[] args) {
        try {
            ServerSocket ss = new ServerSocket(1234);
            System.out.println("Waiting for a clients on " + ss.getLocalPort() + "...");

            // serve clients
            while (true) {
                new Thread(new ServingRunnable(ss.accept())).start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
