package domain;

import java.util.ArrayList;

public class Player {

  public String name;
  public int score;
  public ArrayList<Integer> deck;

  public Player() {

  }

  public Player(String name) {
    this.name = name;
    this.score = 0;
  }

}
