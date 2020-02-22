package master;

import domain.Game;
import service.GameService;
import service.IBackupAdapter;
import service.backup.LocalBackupService;
import service.backup.MongoDBService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

  public static final String DEFAULT_ADDRESS = "localhost";
  public static final int DEFAULT_PORT = 4242;

  private SetupState setupState = SetupState.WAITING_2;

  private ServerSocket serverSocket;

  private ArrayList<Game> activeGames;

  public Server(int port) {
    // make mongodb quieter
    Logger mongoLogger = Logger.getLogger( "org.mongodb.driver" );
    mongoLogger.setLevel(Level.SEVERE);

    try {
      activeGames = new ArrayList<>();
      serverSocket = new ServerSocket(port);
      System.out.println("Listening on port: " + DEFAULT_PORT);
    } catch (IOException e) {
      e.printStackTrace();
    }

    Timer t = new Timer();
    t.schedule(new TimerTask() {
      @Override
      public void run() {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        System.out.println(dateFormat.format(date) + " - Checking if sync is needed...");
        IBackupAdapter mongoAdapter = new MongoDBService();
        IBackupAdapter localAdapter = new LocalBackupService();
        updateBackups(mongoAdapter);
        updateBackups(localAdapter);
      }
    }, 0, 30 * 1000);

    while (true) {
      waitForConnections();
    }
  }

  public void stop() {
    try {
      serverSocket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void waitForConnections() {
    ClientHandler client1Handler = null;
    ClientHandler client2Handler = null;
    Game game = null;
    while (true) {
      try {
        Socket socket = serverSocket.accept();
        if (setupState == SetupState.WAITING_2) {
          game = initiateGame();
          setupState = SetupState.WAITING_1;
          System.out.println("First client connected.");
          client1Handler = connectClient(socket, game);
        } else if (setupState == SetupState.WAITING_1) {
          if (!client1Handler.getClientSocket().isConnected()) {
            // detect if client 1 has disconnected while waiting for client2
            //   assign new connection as client1 if so
            client1Handler.close();
            game = initiateGame();
            setupState = SetupState.WAITING_1;
            System.out.println("First client connected.");
            client1Handler = connectClient(socket, game);
          } else {
            client2Handler = connectClient(socket, game);
            setupState = SetupState.READY;
            System.out.println("Second client connected.");
          }
        }

        if (setupState == SetupState.READY) {
          activeGames.add(game);
          System.out.println("Started game.");
          setupState = SetupState.WAITING_2;

          return;
        }

      } catch (IOException e) {
        e.printStackTrace();
      }
    }

  }

  private void updateBackups(IBackupAdapter backupService) {
    ArrayList<Game> toDelete = new ArrayList<>();
    for (Game game : activeGames) {
      if (backupService.syncNeeded(game)){
        backupService.updateGameState(game);
      } else if (game.isOver()) {
        System.out.println("Deleting game with id: " + game.gameId);
        backupService.deleteGame(game);
        toDelete.add(game);
      }
    }
    for (Game gameToDelete : toDelete) {
      activeGames.remove(gameToDelete);
    }
  }

  private Game initiateGame() {
    Game game = new Game();
    ArrayList<ArrayList<Integer>> decks = GameService.generateDecks();
    game.deck1 = decks.get(0);
    game.deck2 = decks.get(1);
    return game;
  }

  private ClientHandler connectClient(Socket socket, Game game) {
    ClientHandler handler = new ClientHandler(socket);
    handler.setGame(game);
    handler.start();
    return handler;
  }

}
