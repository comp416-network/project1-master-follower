import java.util.Scanner;

public class Main {

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    System.out.println("War game master/follower application started.");
    System.out.print("Run as master? ('y' for master, 'n' for follower): ");
    String masterResponse = scanner.next();
    if (masterResponse.equals("y")) {
      System.out.println("Running as master.");
    } else if (masterResponse.equals("n")) {
      System.out.println("Running as follower.");
    } else {
      System.out.println("Unrecognized input. Terminating.");
    }

  }

}
