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
      out.println("Connected to the game with id: " + game.id);
    } catch (IOException e) {
      e.printStackTrace();
    }

    out.print("Enter name: ");
    try {
      String name = in.readLine();

      player = new Player(name);
      game.addPlayer(player);
    } catch (IOException e) {
      e.printStackTrace();
    }


//    try {
//      clientSocket.close();
//      in.close();
//      out.close();
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
  }

  public void setGame(Game game) {
    this.game = game;
  }

  public Game getGame() {
    return game;
  }

}
