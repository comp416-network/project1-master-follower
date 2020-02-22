package service.backup;

import com.google.gson.Gson;
import domain.Game;
import service.IBackupAdapter;

import java.io.*;

public class LocalBackupService implements IBackupAdapter {

  @Override
  public boolean syncNeeded(Game localGame) {
    // remote to mean 'not being operated on'
    Game remoteGame = findGame(localGame);
    return !localGame.equals(remoteGame);
  }

  @Override
  public Game findGame(Game game) {
    Gson gson = new Gson();

    Game result;
    try {
      FileReader jsonReader = new FileReader("storage/" + getFileNameFromGame(game));
      result = gson.fromJson(jsonReader, Game.class);
    } catch (FileNotFoundException e) {
      result = null;
    }

    return result;
  }

  @Override
  public void updateGameState(Game game) {
    Gson gson = new Gson();

    try {
      FileWriter jsonWriter = new FileWriter("storage/" + getFileNameFromGame(game));
      gson.toJson(game, jsonWriter);
      System.out.println("[LOCAL] Updated game: " + game.gameId);
      jsonWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void deleteGame(Game game) {
    File file = new File("storage/" + getFileNameFromGame(game));
    file.delete();
  }

  private String getFileNameFromGame(Game game) {
    return game.player1.name + "-" + game.player2.name + ".json";
  }

}
