import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;


/**
 * Defines a thread that handles the connection with one client.
 */
public class ConnectionHandler extends Thread implements Serializable{
  private Socket client; // The connection to the client.
  private InputStream inStream;
  private OutputStream outStream;
  private ObjectOutputStream objOut;

  // booleans for operation
  private boolean active = true;
  private boolean disconnect = false;
  private boolean threadDead = false;
  
  private ArrayList<Integer> currPacket;
  private Player player;


  public ConnectionHandler(Socket socket) {
    this.client = socket;
    this.currPacket = new ArrayList<Integer>();
  }

  public void run() {

    // setup connection
    String clientAddress = client.getInetAddress().toString();
    try {
      System.out.println("Connection from " + clientAddress);
      PowerGridServerMain.addConnection(this);
      this.inStream = client.getInputStream();
      this.outStream = client.getOutputStream();
    } catch (Exception e) {
      System.out.println("error in connection handler thread");
    }
    player = new Player(PowerGridServerMain.nextPlayerID);
    PowerGridServerMain.nextPlayerID++;
    PowerGridServerMain.gamestate.addPlayer(player);
    DataInputStream dataIn = new DataInputStream(inStream);
    try {
      objOut = new ObjectOutputStream(outStream);
    } catch (IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    // loop
    while (active) {
      if (!disconnect) {
        try {
          readIn(dataIn.readInt());
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      // wait to save resources
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
      }
    }

    // close connection
    try {
      client.close();
      System.out.println("disconnected " + clientAddress);
    } catch (IOException e) {
      System.out.println("client disconnect error");
    }
    // end of thread
    threadDead = true;

  }
  
  public void readIn(int data) {
    if(data == -1) {
      PowerGridServerMain.addPacket(currPacket);
      this.currPacket = new ArrayList<Integer>();
      this.currPacket.add(player.getPlayerID());
      
    }
    else {
      this.currPacket.add(data);
    }
  }

  public void sendGamestate(Gamestate gamestate) {
   try {
    objOut.writeObject(gamestate);
  } catch (IOException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
  }
  }

  // getters
  public boolean getActive() {
    return active;
  }

  public boolean getDisconnect() {
    return disconnect;
  }

  public boolean getThreadDead() {
    return threadDead;
  }




  // setters
  public void setActive(boolean active) {
    this.active = active;
  }


}
