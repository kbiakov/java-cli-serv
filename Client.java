import java.net.*;
import java.io.*;

public class Client {
    public static void main(String[] args) {
        if ((args.length < 1) || (args.length > 3)) {
            System.out.println("USAGE:");
            System.out.println("Client.java <ip> [<file name in server> [<file name to save>]]");
            return;
        }
        String address = args[0];
        int serverPort = 1234;
        System.out.println("Connecting to " + address + ":" + serverPort + "...");
        Socket socket;
        try {
            socket = new Socket(InetAddress.getByName(address), serverPort);
        } catch (Exception x) {
            System.out.println("Unable to connect to " + address + ":" + serverPort + ".");
            return;
        }
        System.out.println("Connected.\n");
        try {
            InputStream sin = socket.getInputStream();
            OutputStream sout = socket.getOutputStream();

            try {
                byte[] listBuf;
                try {
                    int textLen = 0;
                    for (int i = 0; i < 32; i += 8) textLen |= sin.read() << i; //receive length byte-by-byte big-endian

                    listBuf = new byte[textLen];
                    sin.read(listBuf);

                } catch (Exception x) {
                    return;
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                String servfName;
                if (args.length == 1) {
                    System.out.println(new String(listBuf));
                    System.out.println("Type file name on server:");
                    servfName = in.readLine();
                } else servfName = args[1];

                try {
                    sout.write(servfName.length());
                    sout.write(servfName.getBytes());
                } catch (Exception x) {
                    return;
                }

                long remain = 0;
                for (int i = 0; i < 64; i += 8) remain |= sin.read() << i;//receive file size byte-by-byte big-endian
                if (remain == -1) {
                    System.out.println("404");
                    return;
                }
                System.out.println("Size: " + remain + " bytes");

                String savefName;
                if (args.length < 3) {
                    System.out.println("Type file name to save:");
                    savefName = in.readLine();
                } else savefName = args[2];
                in.close();

                FileOutputStream receiveFile;

                try {
                    receiveFile = new FileOutputStream(savefName);
                } catch (Exception x) {
                    System.out.println("Error: " + x.getMessage());
                    return;
                }

                System.out.println("Receiving \"" + servfName + "\"...");

                try {
                    byte[] buf = new byte[4096];
                    while (remain > buf.length) {
                        int received;
                        try {
                            received = sin.read(buf);
                        } catch (Exception x) {
                            return;
                        }
                        remain -= received;
                        receiveFile.write(buf, 0, received);
                    }

                    try {
                        sin.read(buf, 0, (int) remain);
                    } catch (Exception x) {
                        return;
                    }
                    receiveFile.write(buf, 0, (int) remain);
                    receiveFile.flush();
                } catch (Exception x) {
                    x.printStackTrace();
                } finally {
                    receiveFile.close();
                }
                System.out.println("File \"" + servfName + "\" received.");
            } catch (Exception x) {
                x.printStackTrace();
            } finally {
                sin.close();
                sout.close();
                socket.close();
                System.out.println("Connection closed.");
            }
        } catch (Exception x) {
            x.printStackTrace();
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
