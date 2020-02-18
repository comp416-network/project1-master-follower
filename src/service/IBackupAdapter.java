package service;

import domain.Game;

public interface IBackupAdapter {

  boolean syncNeeded(Game localGame);
  Game findGameById(int id);
  void updateGameState(Game game);

}
