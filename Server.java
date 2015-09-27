import java.net.*;
import java.io.*;

public class Server {
    public static void main(String[] args) {
        try {
            ServerSocket ss = new ServerSocket(1234);
            System.out.println("Waiting for a clients on " + ss.getLocalPort() + "...");
            while (true) {
                new Thread(new ClientRunnable(ss.accept())).start();
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    private static class ClientRunnable implements Runnable {
        private Socket sock;

        public ClientRunnable(Socket sock) {
            this.sock = sock;
        }

        public void run() {
            InputStream sin;
            OutputStream sout;
            System.out.println("Got a client with IP " + sock.getInetAddress() + "\n");

            try {
                sin = sock.getInputStream();
                sout = sock.getOutputStream();
            } catch (Exception x) {
                System.out.println("Client disconnected.");
                try {
                    sock.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }
            try {
                File folder;
                File[] files;

                try {
                    folder = new File("pub");
                    files = folder.listFiles();
                    if (files == null) {
                        throw new IllegalArgumentException();
                    }
                } catch (Exception x) {
                    System.out.println("Error reading folder \"pub\"!");
                    return;
                }

                String s = "";
                for (int i = 0; i < files.length; i++) {
                    if (!files[i].isDirectory()) {
                        s += files[i].getName() + "\n"; //concat names to one string
                    }
                }
                int textLen = s.length();
                byte[] buf = new byte[4096];

                try {
                    for (int i = 0; i < 32; i += 8) {
                        sout.write((byte) (textLen >> i)); //send length byte-by-byte big-endian
                    }
                    sout.write(s.getBytes());
                    textLen = sin.read();
                    sin.read(buf, 0, textLen);

                } catch (IOException x) {
                    //if client disconnected
                    return;
                }

                String fName = new String(buf, 0, textLen);
                System.out.println("Client queries \"" + fName + "\"");

                int i = 0;
                while ((i < files.length) && ((!fName.equals(files[i].getName())) || files[i].isDirectory())) i++;
                if (i == files.length) {
                    System.out.println("404");
                    return;
                }

                long remain;
                FileInputStream sendFile;
                try {
                    remain = files[i].length();
                    sendFile = new FileInputStream(files[i]);
                } catch (Exception x) {
                    System.out.println("Error reading \"" + fName + "\"!");
                    return;
                }

                System.out.println("Size: " + remain + " bytes");

                try {
                    for (i = 0; i < 64; i += 8) {
                        sout.write((byte) (remain >> i)); //send file size byte-by-byte big-endian
                    }
                } catch (Exception x) {
                    return;
                }

                System.out.println("Sending \"" + fName + "\"...");

                try {
                    while (remain > buf.length) {
                        int received;
                        received = sendFile.read(buf);
                        remain -= received;
                        try {
                            sout.write(buf, 0, received);
                        } catch (IOException e) {
                            return;
                        }
                    }
                    sendFile.read(buf, 0, (int) remain);
                    sout.write(buf, 0, (int) remain);
                } catch (Exception x) {
                    x.printStackTrace();
                    return;
                } finally {
                    try {
                        sendFile.close();
                    } catch (Exception x) {
                        x.printStackTrace();
                        return;
                    }
                }
                sout.flush();
                System.out.println("File \"" + fName + "\" sended.");
            } catch (Exception x) {
                x.printStackTrace();
            } finally {
                System.out.println("Client disconnected.");
                try {
                    sin.close();
                    sout.close();
                    sock.close();
                } catch (IOException x) {
                    x.printStackTrace();
                }
            }
        }
    }
}
