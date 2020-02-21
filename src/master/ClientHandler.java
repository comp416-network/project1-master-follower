package master;

import domain.Game;
import domain.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

import static master.Message.*;

public class ClientHandler extends Thread {

  private PrintWriter out;
  private BufferedReader in;
  private Socket clientSocket;

  private Game game;
  private Player player;

  public ClientHandler(Socket clientSocket) {
    this.clientSocket = clientSocket;
    this.game = null;
  }

  @Override
  public void run() {
    try {
      in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      out = new PrintWriter(clientSocket.getOutputStream());
    } catch (IOException e) {
      e.printStackTrace();
    }

    Integer message = -1;
    // handler keeps waiting for new message until
    //    message is null or "quit"
    //    then stops waiting and closes connection
    while (message != QUIT_GAME.getValue()) {
      try {
        // TODO: message error checking on client side
        out.println("Enter command: ");
        out.flush();
        message = Integer.parseInt(in.readLine());
        if (message != QUIT_GAME.getValue()) {

          // handle message
          if (message == WANT_GAME.getValue()) {
            // get player name
            String name = askForName();
            System.out.println("Player ready: " + name);
            player = game.addPlayer(new Player(name));

            // this will be done once when two players are ready
            out.println("Waiting for other player to get ready...");
            out.flush();

            // wait for other player to get ready
            while (true) {
              Thread.sleep(42);
              if (game.isReady()) {
                System.out.println("Sending deck to " + player.name);
                out.println("game start");
                out.flush();
                for (Integer card : player.deck) {
                  out.println(card);
                }
                out.flush();
                break;
              }
            }

          } else if (message == PLAY_CARD.getValue()) {
            System.out.println("received play card message.");
          } else {
            System.out.println("received wrong message.");
          }

        }
      } catch (IOException | InterruptedException e) {
        this.close();
      }
    }

    try {
      System.out.println("Closed connection to player: " + player.name);
      clientSocket.close();
      in.close();
      out.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public boolean isActive() {
    try {
      return in.read() != -1;
    } catch (IOException e) {
      return false;
    }
  }

  public void close() {
    try {
      in.close();
      clientSocket.close();
      out.close();
      this.stop();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private String askForName() {
    out.println("Enter name: ");
    out.flush();
    String name = null;
    try {
      name = in.readLine();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return name;
  }

  // GETTERS & SETTERS

  public void setGame(Game game) {
    this.game = game;
  }

  public Game getGame() {
    return game;
  }

  public Socket getClientSocket() {
    return clientSocket;
  }

}
