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
        ArrayList<String> fileNames = (new LocalBackupService()).getFileNames();
        ArrayList<String> hashes = (ArrayList<String>) fileNames.stream().map(file -> getHash(new File(file)));
        switch (message) {
          case "BACKUP":
            out.println(fileNames.size());
            out.flush();
            for (String hash : hashes) {
              out.println(hash);
            }
            out.flush();
            sentFiles = new ArrayList<>();
          case "RETRANSMIT":
            for (String fileName : sentFiles) {
              sendFile(new File("storage/" + fileName));
            }
          case "CONSISTENCY_CHECK_PASSED":
          case "TRANSMIT":
            int fileCount = Integer.parseInt(in.readLine());
            for (int i = 0; i < fileCount; i++) {
              String hash = in.readLine();
              for (String localHash : hashes) {
                if (localHash.equals(hash)) {
                  int fileIndex = hashes.indexOf(localHash);
                  String fileName = fileNames.get(fileIndex);
                  File file = new File("storage/" + fileName);
                  sentFiles.add(fileName);
                  sendFile(file);
                }
              }
            }
          case "NO_CHANGE":
          default:
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
