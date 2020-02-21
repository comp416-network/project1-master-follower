package master;

import domain.Game;
import domain.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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

    // get player name
    try {
      String name = in.readLine();
      System.out.println("Player entered name: " + name);
      out.println("Hello " + name + "!");
      out.flush();

      player = new Player(name);
      game.addPlayer(player);
    } catch (IOException e) {
      e.printStackTrace();
    }

    String message = "";
    while (message != null && !message.equals("quit")) {
      try {
        message = in.readLine();
        if (message != null) {
          System.out.println("Received message: " + message);
          out.println("Response: " + message);
          out.flush();
        }
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

  public String listenAndRespond() throws IOException {
    String message = in.readLine();
    // do something with message
    return "Received from client: " + message;
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

  public void setGame(Game game) {
    this.game = game;
  }

  public Game getGame() {
    return game;
  }

}
