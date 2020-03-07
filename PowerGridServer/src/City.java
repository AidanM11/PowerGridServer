import java.io.Serializable;

public class City implements Serializable{
  private Player[] housesBuilt;
  private int numHouses;
  private String name;
  private int cityID;
  private static int nextCityID = 0;
  
  public City(String name) {
    super();
    this.name = name;
    this.cityID = nextCityID;
    nextCityID++;
    this.housesBuilt = new Player[3];
    this.numHouses = 0;
  }
  
  public void addHouse(Player player) {
    if(numHouses < 3) {
      housesBuilt[numHouses] = player;
      numHouses++;
    }
    else {
      System.out.println("Tried to build new house when there was no space");
    }
  }
  
  public boolean isHouseAvailable(int phase) {
    if(phase > numHouses) {
      return true;
    }
    return false;
  }
  
  public int priceToBuild(int phase) {
    return 5 + (5 * phase);
  }

  public Player[] getHousesBuilt() {
    return housesBuilt;
  }

  public int getNumHouses() {
    return numHouses;
  }

  public String getName() {
    return name;
  }

  public int getCityID() {
    return cityID;
  }
  
  
  
  
  
}
