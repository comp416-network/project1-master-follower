package domain;

import java.beans.Transient;

public class Game {

  public static int nextId = 1;

  public final int id;
  public Player player1;
  public Player player2;
  public int rounds;

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



}
