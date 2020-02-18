package service.backup;

import domain.Game;
import service.IBackupAdapter;

public class LocalBackupService implements IBackupAdapter {

  @Override
  public boolean syncNeeded(Game localGame) {
    return false;
  }

  @Override
  public Game findGameById(int id) {
    return null;
  }

  @Override
  public void updateGameState(Game game) {

  }

}
