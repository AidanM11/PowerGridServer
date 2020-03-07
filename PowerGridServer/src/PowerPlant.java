import java.io.Serializable;

enum Resource {
  COAL,OIL,NUKE,TRASH,RENEWABLE;
}
public class PowerPlant implements Serializable{
  Gamestate gamestate;
  private Resource[] resType;
  private int activateCost;
  private int numberCitiesPowered;
  private int purchaseCost;
  private Player owner;
  
  public PowerPlant(Gamestate gamestate, Resource[] resType, int activateCost,
      int numberCitiesPowered, int purchaseCost) {
    super();
    this.gamestate = gamestate;
    this.resType = resType;
    this.activateCost = activateCost;
    this.numberCitiesPowered = numberCitiesPowered;
    this.purchaseCost = purchaseCost;
  }
  
  public boolean canBePoweredBy(Resource res) {
    for (int i = 0; i < resType.length; i++) {
      if(res == resType[i]) {
        return true;
      }
    }
    return false;
  }
  
  public Gamestate getGamestate() {
    return gamestate;
  }
  public Resource[] getResType() {
    return resType;
  }
  public int getActivateCost() {
    return activateCost;
  }
  public int getNumberCitiesPowered() {
    return numberCitiesPowered;
  }
  public int getPurchaseCost() {
    return purchaseCost;
  }
  public Player getOwner() {
    return owner;
  }
  
  
  
  
  
  
  
}
