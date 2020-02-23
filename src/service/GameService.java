package service;

import java.util.ArrayList;
import java.util.Collections;

public class GameService {

  /**
   * Generate player decks containing 26 integers representing cards
   * @return an ArrayList holding two ArrayLists that are the decks themselves
   */
  public static ArrayList<ArrayList<Integer>> generateDecks() {
    ArrayList<Integer> nums = new ArrayList<>();
    ArrayList<ArrayList<Integer>> decks = new ArrayList<>();
    for (int i = 0; i < 52; i++) {
      nums.add(i);
    }
    Collections.shuffle(nums);

    decks.add(new ArrayList<>(nums.subList(0, 26)));
    decks.add(new ArrayList<>(nums.subList(26, 52)));

    return decks;
  }

  /**
   * compare two card values (indexed from 0 to 51)
   * @param c1 an integer representing the first card
   * @param c2 an integer representing the second card
   * @return -1 if c1 is greater, +1 if c2 is greater, 0 if tie
   */
  public static int compareCards(int c1, int c2) {
    int score1 = c1 % 13;
    int score2 = c2 % 13;
    return Integer.compare(score2, score1);
  }

}
