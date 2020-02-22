package domain;

import master.ClientHandler;
import master.GameListener;
import master.GameState;
import service.GameService;

import java.util.ArrayList;

public class Game {

  private transient static int nextId = 1;

  public final int id;
  public Player player1;
  public Player player2;
  public int rounds;

  public GameState state;

  public transient ArrayList<Integer> deck1;
  public transient ArrayList<Integer> deck2;

  public ArrayList<ClientHandler> listeners = new ArrayList<>();

  public Game() {
    state = GameState.IDLE;
    this.id = nextId;
    nextId++;
  }

  public Game(Player player1, Player player2, int rounds) {
    this.player1 = player1;
    this.player2 = player2;
    this.rounds = rounds;
    this.id = nextId;
    nextId++;
  }

  public void addPlayer(Player player) {
    if (player1 == null) {
      player1 = player;
      player1.deck = deck1;
    } else {
      player2 = player;
      player2.deck = deck2;
    }
    System.out.println(player.deck);

  }

  public boolean isReady() {
    return player1 != null && player2 != null;
  }

  public void playCard(Player player, int card) {
    player.nextCard = card;
    if (player1.nextCard >= 0 && player2.nextCard >= 0) {
      Player winner = null;
      int result = GameService.compareCards(player1.nextCard, player2.nextCard);
      if (result < 0) {
        winner = player1;
      } else if (result > 0) {
        winner = player2;
      }

      for (ClientHandler listener : listeners) {
        listener.cardsPlayedAction(winner);
      }
      player1.nextCard = -1;
      player2.nextCard = -1;
    }
  }

}
