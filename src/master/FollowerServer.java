package master;

import config.ServerConfig;
import follower.FollowerClient;

import java.io.IOException;
import java.net.*;

public class FollowerServer extends Thread {

  private ServerSocket socket;

  public FollowerServer() {
    try {
      socket = new ServerSocket(ServerConfig.FOLLOWER_PORT);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void run() {
    while(true) {
      listenForConnections();
    }
  }

  private void listenForConnections() {
    while (true) {
      try {
        Socket clientSocket = socket.accept();
        FollowerHandler handler = new FollowerHandler(clientSocket);
        handler.start();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}
