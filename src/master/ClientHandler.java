package master;

import domain.Game;
import domain.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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

    // this is the main loop that the handler checks for messages
    //  if the isPrompting variable is false, the loop is not executed, but still repeated
    //  it is used to block user input in a way
    while (message != QUIT_GAME.getValue()) {
      try {
        // TODO: message error checking on client side
        send("Enter command (0 -> want game, 2 -> play card, 5 -> quit):");

        message = Integer.parseInt(in.readLine());
        if (message != QUIT_GAME.getValue()) {

          // handle message
          if (message == WANT_GAME.getValue()) {
            String name = askForName();
            System.out.println("Player ready: " + name);
            player = new Player(name);

            send("Waiting for the other player...");
            game.addPlayer(player);

            while (true) {
              sleep(50);
              if (game.isReady()) {
                gameReadyAction();
                break;
              }
            }
          } else if (message == PLAY_CARD.getValue()) {
            send("Enter card: ");
            int card = Integer.parseInt(in.readLine());
            send("Waiting for other player to play a card...");
            game.playCard(player, card);
            player.obtainedResult = false;

            while (true) {
              sleep(50);
              if (game.cardsPlayed()) {
                player.obtainedResult = true;
                Player winner = game.roundWinner();
                cardsPlayedAction(winner);
                break;
              }
            }

            if (game.isOver()) {
              Player gameWinner = game.gameWinner();
              gameEndAction(gameWinner);
            }

          } else {
            System.out.println("received wrong message.");
          }

        }

      } catch (IOException | InterruptedException e) {
        System.out.println("Exception raised in client handler.");
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

  public void gameReadyAction() {
    send(Integer.toString(GAME_START.getValue()));
    for (Integer card : player.deck) {
      out.println(card);
    }
    out.flush();
  }

  public void cardsPlayedAction(Player winner) {
    send(Integer.toString(PLAY_RESULT.getValue()));
    if (player.equals(winner)) {
      player.score++;
      send(Integer.toString(ResultMessage.WIN.getValue()));
    } else if (winner == null) {
      send(Integer.toString(ResultMessage.DRAW.getValue()));
    } else {
      send(Integer.toString(ResultMessage.LOSE.getValue()));
    }
  }

  public void gameEndAction(Player winner) {
    send(Integer.toString(GAME_RESULT.getValue()));
    if (player.equals(winner)) {
      send(Integer.toString(ResultMessage.WIN.getValue()));
    } else if (winner == null) {
      send(Integer.toString(ResultMessage.LOSE.getValue()));
    } else {
      send(Integer.toString(ResultMessage.DRAW.getValue()));
    }
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
    this.game = game;
  }

  public Game getGame() {
    return game;
  }

  public Socket getClientSocket() {
    return clientSocket;
  }

}
