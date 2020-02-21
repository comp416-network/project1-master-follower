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
            out.println("Enter name: ");
            out.flush();
            String name = in.readLine();
            System.out.println("Player entered name: " + name);
            player = game.addPlayer(new Player(name));

            // this will be done once when two players are ready
            out.println("Waiting for other player to get ready...");
            out.flush();
            while (true) {
              if (game.isReady()) {
                for (Integer card : player.deck) {
                  System.out.println("Sent card: " + card);
                  out.write(card);
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
      } catch (SocketException e) {
        this.close();
      } catch (IOException e) {
        e.printStackTrace();
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
    } catch (IOException e) {
      e.printStackTrace();
    }
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
