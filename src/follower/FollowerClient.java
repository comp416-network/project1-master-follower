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

  public FollowerClient() {
    try {
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
          System.out.println("Sent backup msg.");
          out.flush();

          try {
            int fileCount = Integer.parseInt(in.readLine());
            ArrayList<String> hashes = new ArrayList<>();
            for (int i = 0; i < fileCount; i++) {
              String hash = in.readLine();
              hashes.add(hash);
            }
            ArrayList<String> ownHashes = new ArrayList<>();
            ArrayList<String> fileNames = (new LocalBackupService()).getFileNames();
            for (String fileName : fileNames) {
              String hash = getHash(new File("storage/" + fileName));
              ownHashes.add(hash);
            }
            hashes.removeAll(ownHashes);

            System.out.println("Received hashes.");
            out.println("TRANSMIT");
            out.flush();

            out.println(hashes.size());
            out.flush();
            for (String hash : hashes) {
              out.println(hash);
              out.flush();
              File receivedFile = receiveFile();
            }
            out.flush();
            System.out.println("Asked for missing files.");



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

      FileOutputStream foStream = new FileOutputStream("storage/" + receivedFile);

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

//    public void run() {
//      while (true) {
//        String command;
//
//        try {
//          in = new BufferedInputStream(socket.getInputStream());
//          out = new BufferedOutputStream(socket.getOutputStream());
//          ArrayList<String> headerParts = ProtocolUtilities.consumeAndBreakHeader(in);
//          command = headerParts.get(0);
//        } catch (IOException e) {
//          e.printStackTrace();
//          return;
//        } catch (NullPointerException e) {
//          e.printStackTrace();
//          return;
//        }
//
//        if(command.equals("SHA")) {
//          try {
//            fileChecksum=getChecksum();
//          } catch (IOException e) {
//            System.err.println("Connection to client dropped. Failed to send public key.");
//          }
//        } else if(command.equals("FILE TRANSFER")){
//          try {
//            File file = receiveFile();
//
//            System.out.println("Name: " + file.getName());
//            String check = Arrays.toString(getFileChecksum(new File(file.getName())));
//
//            if(check.equals(fileChecksum)) {
//              System.out.println("CONSISTENCY_CHECK_PASSED");
//              out.write("CONSISTENCY_CHECK_PASSED".getBytes("ASCII"));
//              out.flush();
//              socket.close();
//            }else {
//              out.write("FAIL\nunsuccessful transmission\n\n".getBytes("ASCII"));
//              out.flush();
//              socket.close();}
//          } catch (GeneralSecurityException e) {
//            e.printStackTrace();
//            return;
//          } catch (IOException e) {
//            e.printStackTrace();
//            System.err.println("Connection to client dropped.");
//            return;
//          }
//
//        } else if (command.equals("RETRANSMIT")){
//          System.out.println("RETRANSMIT");
//
//        } else System.out.println("Invalid command detected: " + command);
//      }
//      }

}
