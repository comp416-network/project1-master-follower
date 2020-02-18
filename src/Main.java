import domain.Game;
import domain.Player;
import service.backup.MongoDBService;

public class Main {

  public static void main(String[] args) {

    Player player1 = new Player();
    Player player2 = new Player();
    player1.name = "ABC";
    player2.name = "DEF";
    player1.score = 10;
    player2.score = 20;

    Game game = new Game();
    game.player1 = player1;
    game.player2 = player2;
    game.rounds = 15;

    MongoDBService service = new MongoDBService();
    service.updateGameState(game);

  }

}
