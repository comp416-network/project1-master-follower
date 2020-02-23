package follower;

import service.ProtocolUtilities;
import service.backup.LocalBackupService;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;


public class FollowerClient {
  private static final int PORT = 8181;
  static String fileChecksum;

  private PrintWriter out;
  private BufferedReader in;

  private BufferedInputStream bIn;
  private BufferedOutputStream bOut;

  private Socket socket;

  private static int nextId = 1;
  private int id;

  public FollowerClient() {
    try {
      this.id = nextId;
      nextId++;

      this.socket = new Socket("localhost", PORT);
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      out = new PrintWriter(socket.getOutputStream());

      bIn = new BufferedInputStream(socket.getInputStream());
      bOut = new BufferedOutputStream(socket.getOutputStream());

      Timer t = new Timer();
      t.schedule(new TimerTask() {
        @Override
        public void run() {
          out.println("BACKUP");
          System.out.println("Sent BACKUP message to master.");
          out.flush();

          try {
            int fileCount = Integer.parseInt(in.readLine());
            ArrayList<String> hashes = new ArrayList<>();
            for (int i = 0; i < fileCount; i++) {
              String hash = in.readLine();
              hashes.add(hash);
            }
            ArrayList<String> ownHashes = new ArrayList<>();
            ArrayList<String> fileNames = (new LocalBackupService()).getFileNames("Follower" + id);
            for (String fileName : fileNames) {
              String hash = getHash(new File(fileName));
              ownHashes.add(hash);
            }
            hashes.removeAll(ownHashes);

            System.out.println("Received hashes from master.");
            System.out.println("Initiating TRANSMIT...");
            out.println("TRANSMIT");
            out.flush();

            out.println(hashes.size());
            out.flush();
            for (String hash : hashes) {
              out.println(hash);
              out.flush();
              File receivedFile = receiveFile();
              String receivedHash = in.readLine();
              while (!getHash(receivedFile).equals(receivedHash)) {
                out.println("RETRANSMIT");
                out.flush();
                receivedFile = receiveFile();
                receivedHash = in.readLine();
                System.out.println("Error receiving file: " + receivedFile.getName());
                System.out.println("\tTrying again...");
              }
              out.println("CONSISTENCY_CHECK_PASSED");
              System.out.println("Successfully received file: " + receivedFile.getName());
            }
            out.flush();
            System.out.println("Asked for " + hashes.size() + " missing files.");

          } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
          }
        }
      }, 0, 10 * 1000);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

    private String scanLineFromStream(InputStream in) throws IOException {
      StringBuilder line = new StringBuilder();
      char c;
      while ((c = (char) in.read()) != '\n') {
        line.append(c);
      }
      return line.toString();
    }

    private File receiveFile() throws GeneralSecurityException, IOException {

      String fileName = scanLineFromStream(bIn);
      String fileSize = scanLineFromStream(bIn);
      File receivedFile = new File(fileName.toString());

      FileOutputStream foStream = new FileOutputStream("Follower" + id + "/" + receivedFile);

      ProtocolUtilities.sendBytes(bIn, foStream, Long.parseLong(fileSize));

      foStream.flush();
      foStream.close();
      return receivedFile;
    }

    private String getChecksum() throws IOException{
      BufferedReader br = new BufferedReader(new InputStreamReader(bIn));
      return br.readLine();
    }

    private String getHash(File file) {
      return Arrays.toString(getFileChecksum(file));
    }

    public byte[] getFileChecksum(File input) {
      try (InputStream in = new FileInputStream(input)) {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] block = new byte[4096];
        int length;
        while ((length = in.read(block)) > 0) {
          digest.update(block, 0, length);
        }
        return digest.digest();
      } catch (Exception e) {
        e.printStackTrace();
      }
      return null;
    }

}
