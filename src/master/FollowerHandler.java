package master;

import service.ProtocolUtilities;
import service.backup.LocalBackupService;

import java.io.*;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;

public class FollowerHandler extends Thread {

  private static String hostName;
  private static int portNumber;

  private Socket socket;
  private BufferedReader in;
  private PrintWriter out;

  public FollowerHandler(Socket socket) {
    this.socket = socket;
  }

  @Override
  public void run() {
    try {
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      out = new PrintWriter(socket.getOutputStream());
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      ArrayList<String> sentFiles = new ArrayList<>();
      while (true) {
        String message = in.readLine();
        ArrayList<String> fileNames = (new LocalBackupService()).getFileNames("storage");
        ArrayList<String> hashes = new ArrayList<>();
        for (String fileName : fileNames) {
          hashes.add(getHash(new File(fileName)));
        }

        switch (message) {
          case "BACKUP":
            System.out.println("Received BACKUP message from follower.");
            out.println(fileNames.size());
            out.flush();
            for (String hash : hashes) {
              out.println(hash);
            }
            out.flush();
            sentFiles = new ArrayList<>();
            break;
          case "TRANSMIT":
            int fileCount = Integer.parseInt(in.readLine());
            System.out.println("Sending file hashes to follower.");
            for (int i = 0; i < fileCount; i++) {
              String hash = in.readLine();
              for (String localHash : hashes) {
                if (localHash.equals(hash)) {
                  int fileIndex = hashes.indexOf(localHash);
                  String fileName = fileNames.get(fileIndex);
                  sentFiles.add(fileName);
                }
              }
              for (String fileName : sentFiles) {
                File file = new File(fileName);
                sendFile(file);
                out.println(getHash(file));
                out.flush();
                String response = in.readLine();
                while (response.equals("RETRANSMIT")) {
                  sendFile(file);
                  out.println(getHash(file));
                  out.flush();
                  response = in.readLine();
                }
                System.out.println("Successfully sent file: " + file);
              }
            }
            break;
          case "NO_CHANGE":
            break;
          default:
            break;
        }
      }


    } catch (IOException e) {
      e.printStackTrace();
    } catch (GeneralSecurityException e) {
      e.printStackTrace();
    }
  }

  private void send(String message) {
    out.println(message);
    out.flush();
  }

  private void sendFile(File file) throws FileNotFoundException, IOException, GeneralSecurityException {
    BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());

    String fileNameAndSize = new String(file.getName()  + "\n" + file.length() + "\n");
    ByteArrayInputStream fileInfoStream = new ByteArrayInputStream(fileNameAndSize.getBytes("ASCII"));
    ProtocolUtilities.sendBytes(fileInfoStream,out);
    FileInputStream fileStream = new FileInputStream(file);
    ProtocolUtilities.sendBytes(fileStream,out);

    //out.write("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n".getBytes("ASCII"));
    out.flush();
  }

  private String getHash(File file) {
    return Arrays.toString(getFileChecksum(file));
  }

  public static byte[] getFileChecksum(File input) {
    try (InputStream in = new FileInputStream(input)) {
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
