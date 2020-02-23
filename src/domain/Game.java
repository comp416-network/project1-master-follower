package domain;

import service.GameService;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class Game {

  public final int gameId;
  public Player player1;
  public Player player2;
  public int rounds;

  public transient ArrayList<Integer> deck1;
  public transient ArrayList<Integer> deck2;

  private transient Random random = new Random();
  private transient boolean isOver;

  public Game() {
    this.gameId = random.nextInt(10000000);
    isOver = false;
  }

  public Game(Player player1, Player player2, int rounds) {
    this.player1 = player1;
    this.player2 = player2;
    this.rounds = rounds;
    this.gameId = random.nextInt(10000000);
    isOver = false;
  }

  /**
   * add a player to the game and assign its deck
   * @param player
   */
  public void addPlayer(Player player) {
    if (player1 == null) {
      player1 = player;
      player1.deck = deck1;
    } else {
      player2 = player;
      player2.deck = deck2;
    }
  }

  /**
   *
   * @return true if both players have connected to the game.
   */
  public boolean isReady() {
    return player1 != null && player2 != null;
  }

  /**
   *
   * @return true if both players has played their cards for the round.
   */
  public boolean cardsPlayed() {
    if (!isOver) {
      return player1.nextCard != -1 && player2.nextCard != -1;
    }
    return true;
  }

  /**
   * Determines the winner of the current round and prepares the game for the next round.
   * @return the winner of this round, null if tie.
   */
  public Player roundWinner() {
    Player winner = null;
    if (!isOver) {
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
    }

    return winner;
  }

  /**
   * Determines the winner of the game.
   * @return the winner of the game
   */
  public Player gameWinner() {
    if (player1 == null) {
      return player2;
    } else if (player2 == null) {
      return player1;
    } else {
      if (player1.score > player2.score) {
        return player1;
      } else if (player2.score > player1.score) {
        return player2;
      }
    }

    printScore();
    return null;
  }

  /**
   *
   * @return true if enough rounds has been played, false otherwise.
   */
  public boolean isOver() {
    return rounds == 3 || isOver;
  }

  /**
   *
   * @param player Player playing the card.
   * @param card Integer value of the card being played.
   */
  public void playCard(Player player, int card) {
    player.nextCard = card;
    player.deck.remove((Integer) card);
    if (cardsPlayed()) {
      rounds++;
    }
  }

  public void playerQuit(Player player) {
    isOver = true;
    if (player.equals(player1)) {
      player1 = null;
    } else if (player.equals(player2)) {
      player2 = null;
    }
  }

  private void printScore() {
    System.out.println(player1.score + " - " + player2.score);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Game)) return false;
    Game game = (Game) o;
    return rounds == game.rounds &&
            isOver == game.isOver &&
            Objects.equals(deck1, game.deck1) &&
            Objects.equals(deck2, game.deck2);
  }

}
