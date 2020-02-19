package service.backup;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import config.MongoDBConfig;
import domain.Game;
import service.IBackupAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class MongoDBService implements IBackupAdapter {

  private static final MongoCollection<Game> gamesCollection = MongoDBConfig.getDatabase().getCollection("games", Game.class);

  /**
   * Check if local copy of game matches with remote copy.
   * @param localGame
   * @return true if sync needed, false otherwise
   */
  @Override
  public boolean syncNeeded(Game localGame) {
    Game remoteGame = findGame(localGame);
    return !localGame.equals(remoteGame);
  }

  /**
   * Writes game to MongoDB. Overwrites if game exists.
   * @param game
   */
  @Override
  public void updateGameState(Game game) {
    List<Integer> ids = getGameIds();
    if (ids.contains(game.id)) {
      gamesCollection.deleteOne(eq("id", game.id));
      System.out.println("Updated game with id: " + game.id);
    } else {
      System.out.println("Created new game with id: " + game.id);
    }
    gamesCollection.insertOne(game);
  }

  @Override
  public void deleteGame(Game game) {
    int id = game.id;
    gamesCollection.deleteOne(eq("id", id));
  }

  /**
   * Returns backup copy of given game object.
   * @param game
   * @return backup game data
   */
  @Override
  public Game findGame(Game game) {
    int id = game.id;
    return (Game) gamesCollection.find(eq("id", id));
  }

  /**
   * Obtains all backup game ids.
   * @return list if game ids
   */
  private List<Integer> getGameIds() {
    List<Integer> ids = new ArrayList<>();
    try (MongoCursor<Game> cursor = gamesCollection.find().iterator()) {
      while (cursor.hasNext()) {
        Game game = cursor.next();
        ids.add(game.id);
      }
    }
    return ids;
  }

}
