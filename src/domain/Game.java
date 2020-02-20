package domain;

public class Game {

  private transient static int nextId = 1;

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

  public void addPlayer(Player player) {
    if (player1 == null) {
      player1 = player;
    } else {
      player2 = player;
    }
  }

}
