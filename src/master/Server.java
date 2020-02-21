package master;

import domain.Game;
import service.GameService;
import service.IBackupAdapter;
import service.backup.LocalBackupService;
import service.backup.MongoDBService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Set;

public class Server {

  public static final String DEFAULT_ADDRESS = "localhost";
  public static final int DEFAULT_PORT = 4242;

  private SetupState setupState = SetupState.WAITING_2;

  private ServerSocket serverSocket;

  private ArrayList<Game> activeGames;

  public Server(int port) {
    try {
      activeGames = new ArrayList<>();
      serverSocket = new ServerSocket(port);
      System.out.println("Listening on port: " + DEFAULT_PORT);
    } catch (IOException e) {
      e.printStackTrace();
    }

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
          game = new Game();
          setupState = SetupState.WAITING_1;
          System.out.println("First client connected.");
          client1Handler = connectClient(socket, game);
        } else if (setupState == SetupState.WAITING_1) {
          if (!client1Handler.isActive()) {
            // detect if client 1 has disconnected while waiting for client2
            //   assign new connection as client1 if so
            client1Handler.close();
            game = new Game();
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
          ArrayList<ArrayList<Integer>> decks = GameService.generateDecks();
          game.deck1 = decks.get(0);
          game.deck2 = decks.get(1);
          System.out.println("Started game.");
          setupState = SetupState.WAITING_2;
          return;
        }

      } catch (IOException e) {
        e.printStackTrace();
      }
    }

  }

  private void backupModifiedGames() {
    IBackupAdapter backupService = new LocalBackupService();
    activeGames.forEach(game -> {
      if (backupService.syncNeeded(game)) {
        backupService.updateGameState(game);
        System.out.println("Updated game with id: " + game.id);
      }
    });
  }

  private ClientHandler connectClient(Socket socket, Game game) {
    ClientHandler handler = new ClientHandler(socket);
    handler.setGame(game);
    handler.start();
    return handler;
  }

}
