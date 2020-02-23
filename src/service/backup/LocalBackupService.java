package service.backup;

import com.google.gson.Gson;
import domain.Game;
import domain.Player;
import service.IBackupAdapter;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
      String gameName = getFileNameFromGame(game);
      if (gameName != null) {
        FileWriter jsonWriter = new FileWriter("storage/" + getFileNameFromGame(game));
        gson.toJson(game, jsonWriter);
        System.out.println("[LOCAL] Updated game: " + game.gameId);
        jsonWriter.close();
      }
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
    if (game == null) {
      return null;
    } else  {
      Player player1 = game.player1;
      Player player2 = game.player2;
      if (player1 == null || player2 == null) {
        return null;
      }
    }
    return game.player1.name + "-" + game.player2.name + ".json";
  }

  public ArrayList<String> getFileNames(String directory) {
    try (Stream<Path> walk = Files.walk(Paths.get(directory + "/"))) {
      List<String> result = walk.filter(Files::isRegularFile)
              .map(x -> x.toString()).collect(Collectors.toList());

      return (ArrayList<String>) result;

    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }


}
