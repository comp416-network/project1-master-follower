package master;

import domain.Player;

public interface GameListener {

  void gameReadyAction();
  void cardsPlayedAction(Player winner);
  void gameEndAction(Player winner);

}
