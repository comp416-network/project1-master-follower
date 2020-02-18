package service.backup;

import com.google.gson.Gson;
import domain.Game;
import service.IBackupAdapter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class LocalBackupService implements IBackupAdapter {

  @Override
  public boolean syncNeeded(Game localGame) {
    // remote to mean 'not being operated on'
    Game remoteGame = findGame(localGame);
    return !remoteGame.equals(localGame);
  }

  @Override
  public Game findGame(Game game) {
    Gson gson = new Gson();

    String player1Name = game.player1.name;
    String player2Name = game.player2.name;

    Game result = null;
    try {
      FileReader jsonReader = new FileReader("storage/" + player1Name + "-" + player2Name + ".json");
      result = gson.fromJson(jsonReader, Game.class);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    return result;
  }

  @Override
  public void updateGameState(Game game) {
    Gson gson = new Gson();

    String player1Name = game.player1.name;
    String player2Name = game.player2.name;

    try {
      FileWriter jsonWriter = new FileWriter("storage/" + player1Name + "-" + player2Name + ".json");
      gson.toJson(game, jsonWriter);
      jsonWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
