import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class PowerGridServerMain {
  private static ArrayList<ConnectionHandler> connections = new ArrayList<ConnectionHandler>();
  public static int LISTENING_PORT = 42020;
  public static String ipAddress = "localhost";
  public static Gamestate gamestate = new Gamestate();
  public static LinkedList<ArrayList<Integer>> packetQueue = new LinkedList<ArrayList<Integer>>();
  public static int nextPlayerID = 0;
  
  public static void main(String[] args) throws UnknownHostException, IOException {
    
    ServerSocket listener = new ServerSocket(LISTENING_PORT, 5, InetAddress.getByName(ipAddress));
    
    System.out.println("Listening on port " + LISTENING_PORT);
    
    //start connection listener
    ConnectionListener connectionListener = new ConnectionListener(listener);
    connectionListener.start();
    Scanner scnr = new Scanner(System.in);
    System.out.println("Press enter when all players connected");
    scnr.nextLine();
    connectionListener.closeListener();
    while(true) {
      if(packetQueue.size() > 0) {
        gamestate.parsePacket(packetQueue.poll());
        //here, tell all connection handlers to send the gamestate
        for(int i = 0; i <connections.size(); i++) {
          connections.get(i).sendGamestate(gamestate);
        }
      }
    }
  }
  
  public static void addConnection(ConnectionHandler connection) {
    connections.add(connection);
    
  }
  
  public static void addPacket(ArrayList<Integer> packet) {
    packetQueue.add(packet);
  }
}
