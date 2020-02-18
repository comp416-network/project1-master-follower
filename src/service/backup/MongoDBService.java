package service.backup;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import config.MongoDBConfig;
import domain.Game;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class MongoDBService {

  private static final MongoCollection<Game> gamesCollection = MongoDBConfig.getDatabase().getCollection("games", Game.class);

  /**
   * Returns true if local copy of the game doesn't match the backup copy.
   * @param localGame
   * @return
   */
  public boolean syncNeeded(Game localGame) {
    Game remoteGame = findGameById(localGame.id);
    return !localGame.equals(remoteGame);
  }

  /**
   * Writes game to MongoDB. Overwrites if game exists.
   * @param game
   */
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

  /**
   * Returns backup copy of game with id.
   * @param id
   * @return
   */
  public Game findGameById(int id) {
    return (Game) gamesCollection.find(eq("id", id));
  }

  /**
   * Obtains all backup game ids.
   * @return
   */
  public List<Integer> getGameIds() {
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
