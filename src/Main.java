import follower.FollowerClient;
import master.FollowerServer;
import master.MasterServer;

import java.util.Scanner;

public class Main {

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    System.out.println("War game master/follower application started.");
    System.out.print("Run as (m)aster or (f)ollower?: ");
    String response = scanner.next();
    if (response.equals("m") || response.equals("master")) {
      System.out.println("Running as master.");
      MasterServer masterServer = new MasterServer();
      FollowerServer followerServer = new FollowerServer();
      followerServer.start();
      masterServer.start();
    } else if (response.equals("f") || response.equals("follower")) {
      System.out.println("Running as follower.");
      FollowerClient client = new FollowerClient();
    } else {
      System.err.println("Unrecognized input. Terminating.");
    }

  }

}
