package domain;

import master.ClientHandler;
import service.GameService;

import java.util.ArrayList;
import java.util.Random;

public class Game {

  public final int gameId;
  public Player player1;
  public Player player2;
  public int rounds;

  public transient ArrayList<Integer> deck1;
  public transient ArrayList<Integer> deck2;

  public transient ArrayList<ClientHandler> listeners = new ArrayList<>();
  private transient Random random = new Random();

  public Game() {
    this.gameId = random.nextInt(10000000);
  }

  public Game(Player player1, Player player2, int rounds) {
    this.player1 = player1;
    this.player2 = player2;
    this.rounds = rounds;
    this.gameId = random.nextInt(10000000);
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

  public boolean cardsPlayed() {
    return player1.nextCard != -1 && player2.nextCard != -1;
  }

  public Player roundWinner() {
    Player winner = null;
    int result = GameService.compareCards(player1.nextCard, player2.nextCard);
    if (result < 0) {
      winner = player1;
    } else if (result > 0) {
      winner = player2;
    }

    if (player1.obtainedResult && player2.obtainedResult) {
      player1.nextCard = -1;
      player2.nextCard = -1;
    }

    return winner;
  }

  public Player gameWinner() {
    Player winner = null;
    if (player1.score > player2.score) {
      winner = player1;
    } else if (player2.score > player1.score) {
      winner = player2;
    }
    return winner;
  }

  public boolean isOver() {
    return rounds == 26;
  }

  public void playCard(Player player, int card) {
    player.nextCard = card;
    player.deck.remove((Integer) card);
    if (cardsPlayed()) {
      rounds++;
    }
  }

}
