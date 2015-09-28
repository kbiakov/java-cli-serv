import java.io.*;
import java.net.Socket;

/**
 * @class ClientInstance
 *
 * Instance of client which served by AppServer class.
 */
public class ClientInstance {

    private Socket socket;
    private String[] args;
    private InputStream sin;
    private OutputStream sout;

    public ClientInstance(Socket socket, String[] args) {
        this.socket = socket;
        this.args = args;
    }

    /**
     * Base point to implement after client connection logic.
     * @throws IOException
     */
    public void process() throws IOException {
        sin = socket.getInputStream();
        sout = socket.getOutputStream();

        // receive length byte-by-byte big-endian
        int textLen = 0;
        for (int i = 0; i < 32; i += 8) {
            textLen |= sin.read() << i;
        }

        byte[] listBuf = new byte[textLen];
        int read = sin.read(listBuf);

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String serverFilename = readServerFilename(listBuf, in);

        sout.write(serverFilename.length());
        sout.write(serverFilename.getBytes());

        // receive file size byte-by-byte big-endian
        long remain = 0;
        for (int i = 0; i < 64; i += 8) {
            remain |= sin.read() << i;
        }

        if (remain == -1) {
            throw new Error("404: file with name " + serverFilename + " not found.");
        }

        System.out.println("Size: " + remain + " bytes");

        String saveFilename = readSaveFilename(in);
        in.close();

        try {
            receiveFile(serverFilename, saveFilename, remain);
            System.out.println("File \"" + serverFilename + "\" received.");
        } catch (FileNotFoundException ex) {
            throw new Error("404: file with name " + serverFilename + " not found.");
        }
    }

    /**
     * Returns file name to save.
     * @param in
     * @return
     * @throws IOException
     */
    private String readSaveFilename(BufferedReader in) throws IOException {
        if (args.length < 3) {
            System.out.println("Type file name to save: ");
            return in.readLine();
        } else {
            return args[2];
        }
    }

    /**
     * Returns file name on server.
     * @param listBuf
     * @param in
     * @return
     * @throws IOException
     */
    private String readServerFilename(byte[] listBuf, BufferedReader in) throws  IOException {
        if (args.length != 1) {
            System.out.println(new String(listBuf));
            System.out.println("Type file name on server: ");
            return in.readLine();
        } else {
            return args[1];
        }
    }

    /**
     * Allows client to receive file from server.
     * @param serverFilename
     * @param saveFilename
     * @param remain
     * @throws FileNotFoundException
     */
    private void receiveFile(String serverFilename, String saveFilename, long remain) throws FileNotFoundException {
        FileOutputStream receiveFile = new FileOutputStream(saveFilename);
        System.out.println("Receiving \"" + serverFilename + "\"...");

        try {
            byte[] buf = new byte[4096];
            while (remain > buf.length) {
                int received = sin.read(buf);
                remain -= received;
                receiveFile.write(buf, 0, received);
            }
            sin.read(buf, 0, (int) remain);
            receiveFile.write(buf, 0, (int) remain);
            receiveFile.flush();
        } catch (IOException x) {
            x.printStackTrace();
        } finally {
            try {
                receiveFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
