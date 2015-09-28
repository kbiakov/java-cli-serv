import java.io.*;
import java.net.Socket;

/**
 * @class ServingRunnable
 *
 * Task for serving client by AppServer class.
 */
public class ServingRunnable implements Runnable {

    private Socket sock;
    private InputStream sin;
    private OutputStream sout;

    public ServingRunnable(Socket sock) {
        this.sock = sock;
    }

    public void run() {
        System.out.println("Got a client with IP " + sock.getInetAddress() + "\n");

        try {
            sin = sock.getInputStream();
            sout = sock.getOutputStream();
            serveClient("pub");
        } catch (IllegalFolderException x) {
            System.out.println("Error reading folder \"" + x.getRootFolder() + "\".");
        } catch (IOException x) {
            System.out.println("AppClient disconnected.");
        } finally {
            try {
                sin.close();
                sout.close();
                sock.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Serves client with specified root folder.
     * @param rootFolder
     * @throws IOException
     * @throws IllegalFolderException
     * @throws Error
     */
    private void serveClient(String rootFolder) throws IOException, IllegalFolderException, Error {
        File folder = new File(rootFolder);
        File[] files = folder.listFiles();
        if (files == null) {
            throw new IllegalFolderException(rootFolder);
        }

        String s = "";
        for (int i = 0; i < files.length; i++) {
            if (!files[i].isDirectory()) {
                s += files[i].getName() + "\n"; // concat names to one string
            }
        }
        int textLen = s.length();
        byte[] buf = new byte[4096];

        for (int i = 0; i < 32; i += 8) {
            sout.write((byte) (textLen >> i)); // send length byte-by-byte big-endian
        }
        sout.write(s.getBytes());
        textLen = sin.read();
        sin.read(buf, 0, textLen);

        String filename = new String(buf, 0, textLen);
        System.out.println("AppClient queries \"" + filename + "\"");

        int k = 0;
        while (k < files.length && (!filename.equals(files[k].getName()) || files[k].isDirectory())) k++;
        if (k == files.length) {
            throw new IOException("Error 404: file \"" + filename + "\" not found.");
        }

        try {
            sendFile(files[k], filename, buf);
        } catch (FileNotFoundException ex) {
            throw new IOException("Error 404: file \"" + filename + "\" not found.");
        } catch (IOException ex) {
            throw new IOException("Error reading \"" + filename + "\"!");
        }
    }

    /**
     * Sends file to client. Method may produce FileNotFoundException & IOException.
     * @param file
     * @param filename
     * @param buf
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void sendFile(File file, String filename, byte[] buf) throws FileNotFoundException, IOException {
        long remain = file.length();
        System.out.println("Size: " + remain + " bytes");

        //send file size byte-by-byte big-endian
        for (int i = 0; i < 64; i += 8) {
            sout.write((byte) (remain >> i));
        }

        System.out.println("Sending \"" + filename + "\"...");

        FileInputStream sendFile = new FileInputStream(file);
        while (remain > buf.length) {
            int received = sendFile.read(buf);
            remain -= received;
            sout.write(buf, 0, received);
        }
        sendFile.read(buf, 0, (int) remain);
        sout.write(buf, 0, (int) remain);
        sout.flush();

        System.out.println("File \"" + filename + "\" sended.");
    }
}
