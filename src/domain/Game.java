package domain;

import java.util.ArrayList;

public class Game {

  private transient static int nextId = 1;

  public final int id;
  public Player player1;
  public Player player2;
  public int rounds;

  public transient ArrayList<Integer> deck1;
  public transient ArrayList<Integer> deck2;

  public Game() {
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

  public Player addPlayer(Player player) {
    if (player1 == null) {
      player1 = player;
      player1.deck = deck1;
      return player1;
    } else {
      player2 = player;
      player2.deck = deck2;
      return player2;
    }
  }

  public boolean isReady() {
    return player1 != null && player2 != null;
  }

}
