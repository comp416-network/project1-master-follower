package master;

import domain.Game;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

  public static final String DEFAULT_ADDRESS = "localhost";
  public static final int DEFAULT_PORT = 4242;

  private ServerSocket serverSocket;

  public Server(int port) {
    try {
      serverSocket = new ServerSocket(port);
      System.out.println("Listening on port: " + DEFAULT_PORT);
    } catch (IOException e) {
      e.printStackTrace();
    }

    while (true) {
      listenAndAccept();
    }
  }

  public void stop() {
    try {
      serverSocket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void listenAndAccept() {
    Socket client1Socket;
    Socket client2Socket;
    try {
      System.out.println();
      client1Socket = serverSocket.accept();
      System.out.println("First client (" + client1Socket.getRemoteSocketAddress()
              + ") connected. Waiting for second client...");
      ClientHandler handler1 = new ClientHandler(client1Socket);
      handler1.start();

      client2Socket = serverSocket.accept();
      System.out.println("Second client (" + client2Socket.getRemoteSocketAddress()
              + ") connected. Starting game...");
      ClientHandler handler2 = new ClientHandler(client2Socket);
      handler2.start();

      Game game = new Game();
      handler1.setGame(game);
      handler2.setGame(game);
      System.out.println("Clients connected to game with id: " + game.id);


    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
