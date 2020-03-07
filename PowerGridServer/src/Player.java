import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Player implements Serializable{
  private PowerPlant[] plants;
  private ArrayList<City> cities;
  private int numPlants;
  private int money;
  private HashMap<Resource, Integer> resources;
  private int playerID;
  private int citiesPowered;
  
  public Player(int playerID) {
    this.money = 50;
    this.numPlants = 0;
    this.plants = new PowerPlant[3];
    this.resources = new HashMap<Resource, Integer>();
    this.playerID = playerID;
    this.citiesPowered = 0;
  }
  
  public PowerPlant getHighestValuePlant() {
    int highestValue = 0;
    int highestPlantInd = 0;
    for(int i = 0; i < numPlants; i++) {
      if(plants[i].getPurchaseCost() > highestValue) {
        highestValue = plants[i].getPurchaseCost();
        highestPlantInd = i;
      }
    }
    return plants[highestPlantInd];
  }
  
  public PowerPlant getLowestValuePlant() {
    int lowestValue = Integer.MAX_VALUE;
    int lowestPlantInd = 0;
    for(int i = 0; i < numPlants; i++) {
      if(plants[i].getPurchaseCost() < lowestPlantInd) {
        lowestValue = plants[i].getPurchaseCost();
        lowestPlantInd = i;
      }
    }
    return plants[lowestPlantInd];
  }
  public int getLowestValuePlantInd() {
    int lowestValue = Integer.MAX_VALUE;
    int lowestPlantInd = 0;
    for(int i = 0; i < numPlants; i++) {
      if(plants[i].getPurchaseCost() < lowestPlantInd) {
        lowestValue = plants[i].getPurchaseCost();
        lowestPlantInd = i;
      }
    }
    return lowestPlantInd;
  }
  
  public void addPlant(PowerPlant plant) {
    if(numPlants < 3) {
      plants[numPlants] = plant;
      numPlants++;
    }
    else {
      plants[getLowestValuePlantInd()] = plant;
      this.adjustResourcesForCapacity();
    }
  }
  
  public boolean canActivatePlant(int ind) {
    int possibleRes = 0;
    if(plants[ind] != null) {
      for(int i = 0; i < resources.keySet().size(); i++) {
        //possible point of failure. don't know if this is valid way to iterate through HashMap
        Resource res = (Resource) resources.keySet().toArray()[i];
        if(plants[ind].canBePoweredBy(res)) {
          possibleRes += resources.get(res);
        }
      }
    }
    else {
      return false;
    }
    if(possibleRes >= plants[ind].getActivateCost()) {
      return true;
    }
    else {
      return false;
    }
  }
  
  public void activatePlant(int ind) {
    if(plants[ind] != null) {
      int resLeft = plants[ind].getActivateCost();
      for(int i = 0; i < resources.keySet().size(); i++) {
        //possible point of failure. don't know if this is valid way to iterate through HashMap
        Resource res = (Resource) resources.keySet().toArray()[i];
        if(plants[ind].canBePoweredBy(res)) {
          for(int j = 0; j < resources.get(res); j++) {
            if(resLeft <= 0) {
              citiesPowered += plants[ind].getNumberCitiesPowered();
              return;
            }
            changeResources(res, -1);
            resLeft--;
          }
        }
      }
    }
    else {
      System.out.println("Tried to activate null plant");
    }
  }
  
  public boolean canFitResource(Resource res) {
    int resCapacity = 0;
    for (int i = 0; i < plants.length; i++) {
      if(plants[i].canBePoweredBy(res)) {
        resCapacity += plants[i].getActivateCost() * 2;
      }
    }
    if(resources.get(res) >= resCapacity) {
      return false;
    }
    else {
      return true;
    }
  }
  
  public void adjustResourcesForCapacity() {
    for(int resInd = 0; resInd < resources.keySet().size(); resInd++) {
      Resource res = (Resource) resources.keySet().toArray()[resInd];
      int resCapacity = 0;
      for (int i = 0; i < plants.length; i++) {
        if(plants[i].canBePoweredBy(res)) {
          resCapacity += plants[i].getActivateCost() * 2;
        }
      }
      if(resources.get(res) >= resCapacity) {
        resources.put(res, resCapacity);
      }
    }
  }
  
  public void cashOutCitiesPowered() {
    if(citiesPowered == 0) changeMoney(10);
    if(citiesPowered == 1) changeMoney(22);
    if(citiesPowered == 2) changeMoney(33);
    if(citiesPowered == 3) changeMoney(44);
    if(citiesPowered == 4) changeMoney(54);
    if(citiesPowered == 5) changeMoney(64);
    if(citiesPowered == 6) changeMoney(73);
    if(citiesPowered == 7) changeMoney(82);
    if(citiesPowered == 8) changeMoney(90);
    if(citiesPowered == 9) changeMoney(98);
    if(citiesPowered == 10) changeMoney(105);
    if(citiesPowered == 11) changeMoney(112);
    if(citiesPowered == 12) changeMoney(118);
    if(citiesPowered == 13) changeMoney(124);
    if(citiesPowered == 14) changeMoney(129);
    if(citiesPowered == 15) changeMoney(134);
    if(citiesPowered == 16) changeMoney(138);
    if(citiesPowered == 17) changeMoney(142);
    if(citiesPowered == 18) changeMoney(145);
    if(citiesPowered == 19) changeMoney(148);
    if(citiesPowered == 20) changeMoney(150);
    
    citiesPowered = 0;
  }

  public int getMoney() {
    return money;
  }

  public void setMoney(int money) {
    this.money = money;
  }
  
  public void changeMoney(int amt) {
    this.money += amt;
  }

  public PowerPlant[] getPlants() {
    return plants;
  }

  public int getNumPlants() {
    return numPlants;
  }

  public HashMap<Resource, Integer> getResources() {
    return resources;
  }
  
  public void changeResources(Resource res, int amt) {
    this.resources.put(res, resources.get(res) + amt);
  }

  public int getPlayerID() {
    return playerID;
  }

  public ArrayList<City> getCities() {
    return cities;
  }

  public int getCitiesPowered() {
    return citiesPowered;
  }
  
  
  
  
}
