package service;

import domain.Game;

public interface IBackupAdapter {

  boolean syncNeeded(Game localGame);
  Game findGame(Game game);
  void updateGameState(Game game);
  void deleteGame(Game game);

}
