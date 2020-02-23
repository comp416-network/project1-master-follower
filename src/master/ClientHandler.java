package master;

import domain.Game;
import domain.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

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

    // the main loop that handles incoming messages
    while (message != QUIT_GAME.getValue()) {
      try {
        // TODO: message error checking on client side
//        send("Enter command (0 -> want game, 2 -> play card, 5 -> quit):");

        String messageString = in.readLine();
        String payload = null;
        String[] arr = new String[2];
        if (messageString == null) {
          this.close();
        } else {
          arr = messageString.split("/", 2);
          if (arr.length == 1) {
            payload = "0";
          } else {
            payload = arr[1];
          }
          message = Integer.parseInt(arr[0]);
        }

        if (message != QUIT_GAME.getValue()) {

          // handle message
          if (message == WANT_GAME.getValue()) {
            String name = payload;
            System.out.println("Player ready: " + name);
            player = new Player(name);

            game.addPlayer(player);

            // wait for the game to be ready
            // invoke gameReadyAction when game is ready
            while (true) {
              sleep(50);
              if (game.isReady()) {
                gameReadyAction();
                break;
              }
            }

          } else if (message == PLAY_CARD.getValue()) {
            if (game.isOver()) {
              Player gameWinner = game.gameWinner();
              gameEndAction(gameWinner);
            }

            int card = Integer.parseInt(payload);
            game.playCard(player, card);
            player.obtainedResult = false;

            // wait for the other player to play a card
            while (true) {
              sleep(50);
              if (game.cardsPlayed()) {
                player.obtainedResult = true;
                Player winner = game.roundWinner();
                cardsPlayedAction(winner);
                break;
              }
            }

            // if game has ended after last round
            if (game.isOver()) {
              Player gameWinner = game.gameWinner();
              gameEndAction(gameWinner);
            }

          } else {
            System.out.println("received wrong message.");
          }

        } else {
          // client sent quit message
          game.playerQuit(player);
          send("6/6"); // arbitrary message
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

  /**
   * Sends the cards to the client. Called when the game is ready.
   */
  public void gameReadyAction() {
    ArrayList<Integer> deck = player.deck;
    StringBuilder deckString = new StringBuilder();
    for (Integer card : deck) {
      deckString.append(card).append("/");
    }
    send(deckString.toString());
    out.flush();
  }

  /**
   * Invoked when both players have played a card.
   * Gets the result and sends the appropriate message to the client.
   * @param winner The player that has won the last round. Null if tie.
   */
  public void cardsPlayedAction(Player winner) {
    if (player.equals(winner)) {
      player.score++;
      if (!game.isOver()) {
        send(PLAY_RESULT.getValue() + "/" + ResultMessage.WIN.getValue());
      }
    } else if (winner == null) {
      if (!game.isOver()) {
        send(PLAY_RESULT.getValue() + "/" + ResultMessage.DRAW.getValue());
      }
    } else {
      if (!game.isOver()) {
        send(PLAY_RESULT.getValue() + "/" + ResultMessage.LOSE.getValue());
      }
    }
  }

  /**
   * Invoked when the game ends. Informs the player of the game result.
   * @param winner The player that has won the game. Null if tie.
   */
  public void gameEndAction(Player winner) {
    if (player.equals(winner)) {
      send(GAME_RESULT.getValue() + "/" + ResultMessage.WIN.getValue());
    } else if (winner == null) {
      send(GAME_RESULT.getValue() + "/" + ResultMessage.DRAW.getValue());
    } else {
      send(GAME_RESULT.getValue() + "/" + ResultMessage.LOSE.getValue());
    }
  }

  // HELPER METHODS

  /**
   * Closes I/O streams and the connection.
   */
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

  /**
   * Sends a message to the client asking for its name.
   * @return The name input by the client.
   */
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

  /**
   * Sends a message and flushes client's output stream.
   * @param message
   */
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
