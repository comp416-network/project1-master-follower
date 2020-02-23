package master;

import config.ServerConfig;
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

public class MasterServer extends Thread {

  /**
   * WAITING_2: Waiting for two players.
   * WAITING_1: Waiting for one player. One player has connected.
   * READY:     Both players have connected.
   */
  private SetupState setupState = SetupState.WAITING_2;

  private ServerSocket clientServerSocket;

  private ArrayList<Game> activeGames;

  public MasterServer() {
    // make mongodb quieter
    Logger mongoLogger = Logger.getLogger( "org.mongodb.driver" );
    mongoLogger.setLevel(Level.SEVERE);

    try {
      activeGames = new ArrayList<>();
      clientServerSocket = new ServerSocket(ServerConfig.CLIENT_PORT);
      System.out.println("Listening on port for clients: " + ServerConfig.CLIENT_PORT);
    } catch (IOException e) {
      e.printStackTrace();
    }

    // sync local/mongodb backup files periodically
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
    }, 0, ServerConfig.SYNC_SECONDS * 1000);

  }

  @Override
  public void run() {
    while (true) {
      waitForConnections();
    }
  }

  private void waitForConnections() {
    ClientHandler client1Handler = null;
    ClientHandler client2Handler = null;
    Game game = null;
    while (true) {
      try {
        Socket clientSocket = clientServerSocket.accept();

        // CLIENT

        if (setupState == SetupState.WAITING_2) {
          // create new game if first player connects
          game = initiateGame();
          setupState = SetupState.WAITING_1;
          System.out.println("First client connected.");
          client1Handler = connectClient(clientSocket, game);
        } else if (setupState == SetupState.WAITING_1) {
          if (!client1Handler.getClientSocket().isConnected()) {
            // detect if client 1 has disconnected while waiting for client2
            //   assign new connection as client1 if so
            client1Handler.close();
            game = initiateGame();
            setupState = SetupState.WAITING_1;
            System.out.println("First client connected.");
            client1Handler = connectClient(clientSocket, game);
          } else {
            // attach second client to existing game
            client2Handler = connectClient(clientSocket, game);
            setupState = SetupState.READY;
            System.out.println("Second client connected.");
          }
        }

        // start game when both has connected
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

  /**
   * Goes through all the active games, updating those that need updating and deleting those that have ended.
   * @param backupService local or mongo db
   */
  private void updateBackups(IBackupAdapter backupService) {
    ArrayList<Game> toDelete = new ArrayList<>();
    for (Game game : activeGames) {
      if (backupService.syncNeeded(game)){
        backupService.updateGameState(game);
      }
      if (game.isOver()) {
        System.out.println("Deleting game with id: " + game.gameId);
        backupService.deleteGame(game);
        toDelete.add(game);
      }
    }
    for (Game gameToDelete : toDelete) {
      activeGames.remove(gameToDelete);
    }
  }

  /**
   * Creates a new game. Deals the cards.
   * @return the created game
   */
  private Game initiateGame() {
    Game game = new Game();
    ArrayList<ArrayList<Integer>> decks = GameService.generateDecks();
    game.deck1 = decks.get(0);
    game.deck2 = decks.get(1);
    return game;
  }

  /**
   * Accept new connection, create new thread and set its game.
   * @param socket client socket that is connecting
   * @param game Game to attach the client
   * @return the ClientHandler object
   */
  private ClientHandler connectClient(Socket socket, Game game) {
    ClientHandler handler = new ClientHandler(socket);
    handler.setGame(game);
    handler.start();
    return handler;
  }

}
