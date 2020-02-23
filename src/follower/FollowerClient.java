package follower;

import config.ServerConfig;
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

      // assigns the follower its id
      this.id = nextId;
      nextId++;

      // initiates connection to the master
      this.socket = new Socket(ServerConfig.ADDRESS, PORT);
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      out = new PrintWriter(socket.getOutputStream());

      bIn = new BufferedInputStream(socket.getInputStream());
      bOut = new BufferedOutputStream(socket.getOutputStream());

      // performs syncronization periodically
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

            // hashes contains hashes that don't belong to the files the follower owns
            hashes.removeAll(ownHashes);

            System.out.println("Received hashes from master.");
            System.out.println("Initiating TRANSMIT...");
            out.println("TRANSMIT");
            out.flush();

            out.println(hashes.size());
            out.flush();

            // ask for file from master for each hash
            for (String hash : hashes) {
              out.println(hash);
              out.flush();
              File receivedFile = receiveFile();
              String receivedHash = in.readLine();
              while (!getHash(receivedFile).equals(receivedHash)) {
                // ask to retransmit file if file has been received erroneously
                out.println("RETRANSMIT");
                out.flush();
                receivedFile = receiveFile();
                receivedHash = in.readLine();
                System.out.println("Error receiving file: " + receivedFile.getName());
                System.out.println("\tTrying again...");
              }
              // alert master is file has been received successfully
              out.println("CONSISTENCY_CHECK_PASSED");
              System.out.println("Successfully received file: " + receivedFile.getName());
            }
            out.flush();
            System.out.println("Asked for " + hashes.size() + " missing files.");

          } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
          }
        }
      }, 0, ServerConfig.SYNC_SECONDS * 1000);

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

  /**
   * Receives file from input stream and saves it to the appropriate directory.
   * @return the file received.
   * @throws IOException
   */
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

  /**
   * Returns hash of the given file.
   * @param file to calculate the hash of
   * @return the hash of the file as a string
   */
    private String getHash(File file) {
      return Arrays.toString(getFileChecksum(file));
    }

  /**
   * Generates checksum from given file
   * @param input
   * @return
   */
    public byte[] getFileChecksum(File input) {
      try (InputStream in = new FileInputStream(new File("Follower" + id + "/" + input.getName()))) {
        MessageDigest digest = MessageDigest.getInstance("SHA1");
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
