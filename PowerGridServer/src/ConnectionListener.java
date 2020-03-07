import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ConnectionListener extends Thread implements Serializable{
  private ServerSocket listener;
  private boolean running = true;
  private boolean gameStarted;
  private ArrayList<InetAddress> allPrevAddresses;
  private HashMap<InetAddress, Player> playersByAddress;
  private int nextPlayerID;

  public ConnectionListener(ServerSocket listener) {
    this.listener = listener;
    this.allPrevAddresses = new ArrayList<InetAddress>();
    this.nextPlayerID = 1;
    this.gameStarted = false;
  }

  public void run() {
    while (running) {
      Socket connection = null;
      try {
        connection = listener.accept();
      } catch (IOException e) {
        if (running) {
          e.printStackTrace();
          System.out.print("oh no error in connection listener");
          continue;
        }
      }
      if (running) {
//        Player p;
//        p = new Player(nextPlayerID);
//        nextPlayerID++;
//        PowerGridServerMain.gamestate.addPlayer(p);
        allPrevAddresses.add(connection.getInetAddress());
        ConnectionHandler handler = new ConnectionHandler(connection);
        handler.start();
      }
    }
  }

  public void closeListener() {
    running = false;
    try {
      listener.close();
    } catch (IOException e) {
      System.out.println("listener close error");
    }
  }

  public void setGameStarted(boolean gameStarted) {
    this.gameStarted = gameStarted;
  }
  
  
  
}
