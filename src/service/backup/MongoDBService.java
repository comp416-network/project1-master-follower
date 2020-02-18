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

  public void updateGameState(Game game) {
    List<Integer> ids = getGameIds();
    if (ids.contains(game.id)) {
      gamesCollection.deleteOne(eq("id", game.id));
    }
    gamesCollection.insertOne(game);
  }

  public Game getGameStateFromId(int id) {
    return (Game) gamesCollection.find(eq("id", id));
  }

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
