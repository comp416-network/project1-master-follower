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
//    List<Integer> ids = getGameIds();
    gamesCollection.deleteOne(eq("gameId", game.gameId));
    gamesCollection.insertOne(game);
    System.out.println("[MONGO DB] Updated game: " + game.gameId);
  }

  @Override
  public void deleteGame(Game game) {
    int id = game.gameId;
    gamesCollection.deleteOne(eq("gameId", id));
  }

  /**
   * Returns backup copy of given game object.
   * @param game
   * @return backup game data
   */
  @Override
  public Game findGame(Game game) {
    int id = game.gameId;
    Game result;
    try {
      result = (Game) gamesCollection.find(eq("gameId", id));
    } catch (ClassCastException e) {
      result = null;
    }
    return result;
  }

}
