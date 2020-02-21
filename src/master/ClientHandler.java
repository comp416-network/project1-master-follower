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

public class ClientHandler extends Thread implements GameListener {

  private PrintWriter out;
  private BufferedReader in;
  private Socket clientSocket;

  private Game game;
  private Player player;

  private boolean isPrompting;

  public ClientHandler(Socket clientSocket) {
    this.clientSocket = clientSocket;
    this.game = null;
    this.isPrompting = true;
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

    // this is the main loop that the handler checks for messages
    //  if the isPrompting variable is false, the loop is not executed, but still repeated
    //  it is used to block user input in a way
    while (message != QUIT_GAME.getValue()) {
      try {
        // TODO: message error checking on client side
        if (isPrompting) {
          send("Enter command: ");
        } else {
          continue;
        }
        message = Integer.parseInt(in.readLine());
        if (message != QUIT_GAME.getValue()) {

          // handle message
          if (message == WANT_GAME.getValue()) {
            // get player name
            String name = askForName();
            System.out.println("Player ready: " + name);
            player = new Player(name);

            send("Waiting for other player...");
            game.addPlayer(player);
            isPrompting = false;


          } else if (message == PLAY_CARD.getValue()) {
            int card = Integer.parseInt(in.readLine());

            send("Waiting for other player to play a card...");
            game.playCard(player, card);
            isPrompting = false;


          } else {
            System.out.println("received wrong message.");
          }

        }
      } catch (IOException e) {
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

  // LISTENER METHODS

  @Override
  public void gameReadyAction() {
    System.out.println("Sending deck to " + player.name);
    send("game start");
    for (Integer card : player.deck) {
      out.println(card);
    }
    out.flush();
    isPrompting = true;
  }

  @Override
  public void cardsPlayedAction(Player winner) {
    if (player.equals(winner)) {
      player.score++;
      send("You win this round!");
    } else if (winner == null) {
      send("It was a tie!");
    } else {
      send("You lose!");
    }
    isPrompting = true;
  }

  // HELPER METHODS

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
    send("Enter name: ");
    String name = null;
    try {
      name = in.readLine();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return name;
  }

  private void send(String message) {
    out.println(message);
    out.flush();
  }

  // GETTERS & SETTERS

  public void setGame(Game game) {
    game.listeners.add(this);
    this.game = game;
  }

  public Game getGame() {
    return game;
  }

  public Socket getClientSocket() {
    return clientSocket;
  }

}
