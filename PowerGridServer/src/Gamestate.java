import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class Gamestate implements Serializable{
  private ArrayList<Player> players;
  private ArrayList<Player> playersInTurnOrder;
  private ArrayList<PowerPlant> plantDeck;
  private ArrayList<PowerPlant> plantsOut;
  private PowerPlant plantUpAuction;
  private int currBid;
  private Player currBidder;
  private ArrayList<Player> playersInAuction;
  private ResourceBoard resBoard;
  private int currPhase;
  private int turnNumber;
  private ArrayList<City> cities;
  private ArrayList<CityConnection> cityConnections;
  private boolean phase3Hit;
  private int plantsAvailable;
  private int citiesNeededPhase2;
  private int citiesNeededWin;
  private Player winner;
  private boolean gameOver;
  //Steps
  //1 - buy powerplants
  //2 - bid on powerplants
  //3 - buy resources
  //4 - buy cities
  //5 - power
  private int gameStep;
  private int playerTurn;
  private int biddingTurn;
  private boolean sendable;
  
  
  public Gamestate() {
    this.players = new ArrayList<Player>();
    this.playersInTurnOrder = new ArrayList<Player>();
    this.plantDeck = new ArrayList<PowerPlant>();
    this.plantsOut = new ArrayList<PowerPlant>();
    this.resBoard = new ResourceBoard(this);
    this.currPhase = 1;
    this.turnNumber = 1;
    this.gameStep = 1;
    this.playerTurn = 0;
    this.plantsAvailable = 4;
    this.sendable = true;
    this.phase3Hit = false;
    this.gameOver = false;
    this.createDeck();
    for(int i = 0; i < 8; i++) {
      this.dealPlant();
    }
    Collections.shuffle(plantDeck);
    //Add phase 3 card
    plantDeck.add(new PowerPlant(null, new Resource[0], 333, 333, 333));
    this.sortPlants();
    this.createCities();
    this.createCityConnections();
  }
  
  /*Packet instruction key (f1, f2 mean following 1/2/etc. integers)
   * first integer is playerID
   * second is gamestep
   * 
   * -1: end packet
   * 1: put up powerplant
   *    f1: value of powerplant, -1 is pass
   * 2: bid on powerplant
   *    f1: 1 is bid, 0 is pass. can only increment bids by one
   * 3: buy resources
   *    f1: type of resource (start at 0, coal, oil, nuke, trash) (in order of enum), if -1, pass
   * 4: buy city
   *    f1: cityID, if -1, pass
   * 5: power plants
   *    f1-3; plants to power, using index as they appear in the Player object
   */
  public void parsePacket(ArrayList<Integer> packet) {
    initializeConditions(players.size());
    sendable = false;
    if(packet.get(1) == 2) {
      if(gameStep == 2 && packet.get(0) == playersInTurnOrder.get(biddingTurn).getPlayerID()) {
        if(packet.get(2) == 1) {
          if(playersInTurnOrder.get(biddingTurn).getMoney() >= currBid + 1) {
            currBid++;
            currBidder = playersInTurnOrder.get(biddingTurn);
            nextBiddingTurn();
            return;
          }
        }
        else {
          playersInAuction.remove(playersInTurnOrder.get(biddingTurn));
          nextBiddingTurn();
        }
      }
      else {
        //handling if bid packet out of step or turn
      }
    }
    if(packet.get(0) == playersInTurnOrder.get(playerTurn).getPlayerID()) {
      //Step 1
      if(packet.get(1) == 1) {
        if(gameStep == 1) {
          if(packet.get(2) == -1) {
            if(turnNumber != 1) {
              nextPlayerTurn();
              return;
            }
            else {
              //handling if player tried to not buy plant on turn 1
            }
          }
          //plantsAvailable beacuse only top row is available to buy
          for(int i = 0; i < plantsAvailable; i++) {
            if(plantsOut.get(i) != null) {
              if(packet.get(2) == plantsOut.get(i).getPurchaseCost()) {
                if(playersInTurnOrder.get(playerTurn).getMoney() >= plantsOut.get(i).getPurchaseCost()) {
                  plantUpAuction = plantsOut.get(i);
                  gameStep = 2;
                  playersInAuction = new ArrayList<Player>();
                  for(int j = 0; j < playersInTurnOrder.size(); j++) {
                    if(j >= playerTurn) {
                      playersInAuction.add(playersInTurnOrder.get(j));
                    }
                  }
                  biddingTurn = playerTurn;
                  currBid = plantsOut.get(i).getPurchaseCost();
                  currBidder = playersInTurnOrder.get(playerTurn);
                  nextBiddingTurn();
                  return;
                }
                else {
                  //handling if player tries to put up plant they can't afford
                }
              }
            }
          }
        }
        else {
          //handling if player submitted packet not relevant to current step
        }
      }
      //Step 2
      if(packet.get(1) == 2) {
        if(gameStep == 2) {
          //if pass
          if(packet.get(2) == 0) {
            //this is actually handled up above, because it is out of playerTurn
          }
        }
        else {
          //handling if player submitted packet not relevant to current step
        }
      }
      //Step 3
      if(packet.get(1) == 3) {
        if(gameStep == 3) {
          if(packet.get(2) == -1) {
            nextPlayerTurn();
            return;
          }
          else if (packet.get(2) >= 0 && packet.get(2) < 4) {
            int resPrice = resBoard.getPriceOfResource(Resource.values()[packet.get(2)]);
            Player p = playersInTurnOrder.get(playerTurn);
            if(p.getMoney() >= resPrice && p.canFitResource(Resource.values()[packet.get(2)])) {
              p.changeMoney(-resPrice);
              p.changeResources(Resource.values()[packet.get(2)], 1);
              resBoard.changeResourceAmount(Resource.values()[packet.get(2)], -1);
            }
            else {
              //handling if player tries to buy resource they can't afford
            }
          }
        }
        else {
          //handling if player submitted packet not relevant to current step
        }
      }
      //Step 4
      //for this step, the server assumes the packet sent contains the correct cost of building the
      //city. check client code for errors involving city cost
      if(packet.get(1) == 4) {
        if(gameStep == 4) {
          if(packet.get(2) == -1) {
            nextPlayerTurn();
            return;
          }
          City tarCity = null;
          for(int i = 0; i < cities.size(); i++ ) {
            if(packet.get(2) == cities.get(i).getCityID()) {
              tarCity = cities.get(i);
              break;
            }
          }
          if(tarCity.isHouseAvailable(currPhase) 
              && playersInTurnOrder.get(playerTurn).getMoney() > packet.get(3) 
              && playersInTurnOrder.get(playerTurn).getCities().contains(tarCity) == false) {
            tarCity.addHouse(playersInTurnOrder.get(playerTurn));
            playersInTurnOrder.get(playerTurn).changeMoney(-packet.get(3));
            playersInTurnOrder.get(playerTurn).getCities().add(tarCity);
          }
          else {
            //handling if player tries to buy invalid city
          }
        }
        else {
          //handling if player submitted packet no relevant to current step
        }
      }
      //Step 5
      if(packet.get(1) == 5) {
        if(gameStep == 5) {
          Player p = playersInTurnOrder.get(playerTurn);
          for(int i = 2; i < packet.size(); i++) {
            int pInd = packet.get(i);
            if(p.canActivatePlant(pInd)) {
              p.activatePlant(pInd);
            }
          }
          nextPlayerTurn();
        }
        else {
          //handling if player submitted packet no relevant to current step
        }
      }
    }
    else {
      //handling if player submitted packet out of turn
    }
    sendable = true;
  }
  
  public void addPlayer(Player player) {
    players.add(player);
  }
  
  public void nextPlayerTurn() {
    if(playerTurn < playersInTurnOrder.size() - 1) {
      playerTurn++;
    }
    else {
      playerTurn = 0;
      nextGameStep();
    }
  }
  public void nextBiddingTurn() {
    if(playersInAuction.size() <= 1) {
      currBidder.addPlant(plantUpAuction);
      currBidder.changeMoney(-currBid);
      plantsOut.remove(plantUpAuction);
      dealPlant();
      gameStep = 1;
      nextPlayerTurn();
      return;
    }
    incrBiddingTurn();
    while(playersInAuction.contains(playersInTurnOrder.get(biddingTurn)) == false) {
      incrBiddingTurn();
    }
  }
  public void incrBiddingTurn() {
    if(biddingTurn < players.size() - 1) {
      biddingTurn++;
    }
    else {
      biddingTurn = 0;
    }
  }
  
  
  public void nextGameStep() {
    //this is to skip the bidding step, which is handled separately
    if(gameStep == 1) {
      gameStep+=2;
      if(turnNumber == 1) {
        sortPlayerTurnOrder();
      }
    }
    else if(gameStep == 5) {
      sortPlants();
      boolean phase2 = false;
      boolean gameEnd = false;
      for(int i = 0; i < players.size(); i++) {
        if(players.get(i).getCities().size() >= citiesNeededPhase2) {
          phase2 = true;
        }
        if(players.get(i).getCities().size() >= citiesNeededWin) {
          gameEnd = true;
        }
      }
      if(gameEnd) {
        determineWinner();
      }
      if(currPhase == 1 && phase2) {
        currPhase = 2;
        plantsOut.remove(plantsOut.size() - 1);
        dealPlant();
      }
      if(currPhase == 3 && phase3Hit == false) {
        phase3Hit = true;
        plantsOut.remove(plantsOut.size() - 1);
        plantsOut.remove(plantsOut.size() - 1);
        plantsAvailable = 6;
      }
      PowerPlant pl = plantsOut.get(plantsOut.size()-1);
      for (int i = 0; i < players.size(); i++) {
        players.get(i).cashOutCitiesPowered();
      }
      plantsOut.remove(pl);
      plantDeck.add(pl);
      gameStep = 1;
      turnNumber++;
    }
    else {
      gameStep++;
    }
  }
  
  
  public void dealPlant() {
    if(plantsOut.get(0).getPurchaseCost() == 333) {
      //phase 3
      currPhase = 3;
    }
    if(plantDeck.isEmpty() == false) {
      plantsOut.add(plantDeck.get(0));
      plantDeck.remove(0);
      sortPlants();
    }
  }
  
  public void sortPlants() {
    for(int i = 0; i < plantsOut.size(); i++) {
      for(int j = 0; j < i; j++) {
        if(plantsOut.get(i).getPurchaseCost() < plantsOut.get(j).getPurchaseCost()) {
          plantsOut.add(j, plantsOut.get(i));
          plantsOut.remove(i+1);
        }
      }
    }
  }
  
  public void initializeConditions(int players) {
    if(players == 2) {
      citiesNeededPhase2 = 10;
      citiesNeededWin = 21;
    }
    if(players > 2 && players < 5) {
      citiesNeededPhase2 = 7;
      citiesNeededWin = 17;
    }
    if(players == 5) {
      citiesNeededPhase2 = 7;
      citiesNeededWin = 15;
    }
    if(players == 6) {
      citiesNeededPhase2 = 6;
      citiesNeededWin = 14;
    }
  }
  
  public void determineWinner() {
    int highNumCitiesPowered = -1;
    ArrayList<Player> highCitiesPowered = new ArrayList<Player>();
    for (int i = 0; i < players.size(); i++) {
      if(players.get(i).getCitiesPowered() > highNumCitiesPowered) {
        highNumCitiesPowered = players.get(i).getCitiesPowered();
        highCitiesPowered = new ArrayList<Player>();
        highCitiesPowered.add(players.get(i));
      }
    }
    if(highCitiesPowered.size() == 1) {
      winner = highCitiesPowered.get(0);
      gameOver = true;
      return;
    }
    int highMoney = -1;
    Player playerHighMoney = null;
    for (int i = 0; i < highCitiesPowered.size(); i++) {
      if(highCitiesPowered.get(i).getMoney() > highMoney) {
        highMoney = highCitiesPowered.get(i).getMoney();
        playerHighMoney = highCitiesPowered.get(i);
      }
    }
    winner = playerHighMoney;
    gameOver = true;
  }
  
  
  public void createDeck() {
    this.plantDeck = new ArrayList<PowerPlant>();
    Resource[] res;
    int purchaseCost, activateCost, numCities;
    
    res = new Resource[] {Resource.OIL};
    purchaseCost = 3;
    activateCost = 2;
    numCities = 1;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.COAL};
    purchaseCost = 4;
    activateCost = 2;
    numCities = 1;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.COAL, Resource.OIL};
    purchaseCost = 5;
    activateCost = 2;
    numCities = 1;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.TRASH};
    purchaseCost = 6;
    activateCost = 1;
    numCities = 1;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.OIL};
    purchaseCost = 7;
    activateCost = 3;
    numCities = 2;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.COAL};
    purchaseCost = 8;
    activateCost = 3;
    numCities = 2;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.OIL};
    purchaseCost = 9;
    activateCost = 1;
    numCities = 1;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.COAL};
    purchaseCost = 10;
    activateCost = 2;
    numCities = 2;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.NUKE};
    purchaseCost = 11;
    activateCost = 1;
    numCities = 2;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.COAL, Resource.OIL};
    purchaseCost = 12;
    activateCost = 2;
    numCities = 2;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.RENEWABLE};
    purchaseCost = 13;
    activateCost = 0;
    numCities = 1;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.TRASH};
    purchaseCost = 14;
    activateCost = 2;
    numCities = 2;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.COAL};
    purchaseCost = 15;
    activateCost = 2;
    numCities = 3;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.OIL};
    purchaseCost = 16;
    activateCost = 2;
    numCities = 3;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.NUKE};
    purchaseCost = 17;
    activateCost = 1;
    numCities = 2;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.RENEWABLE};
    purchaseCost = 18;
    activateCost = 0;
    numCities = 2;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.TRASH};
    purchaseCost = 19;
    activateCost = 2;
    numCities = 3;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.COAL};
    purchaseCost = 20;
    activateCost = 3;
    numCities = 5;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.COAL, Resource.OIL};
    purchaseCost = 21;
    activateCost = 2;
    numCities = 4;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.RENEWABLE};
    purchaseCost = 22;
    activateCost = 0;
    numCities = 2;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.NUKE};
    purchaseCost = 23;
    activateCost = 1;
    numCities = 3;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.TRASH};
    purchaseCost = 24;
    activateCost = 2;
    numCities = 4;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.COAL};
    purchaseCost = 25;
    activateCost = 2;
    numCities = 5;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.OIL};
    purchaseCost = 26;
    activateCost = 2;
    numCities = 5;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.RENEWABLE};
    purchaseCost = 27;
    activateCost = 0;
    numCities = 3;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.NUKE};
    purchaseCost = 28;
    activateCost = 1;
    numCities = 4;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.COAL, Resource.OIL};
    purchaseCost = 29;
    activateCost = 1;
    numCities = 4;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.TRASH};
    purchaseCost = 30;
    activateCost = 3;
    numCities = 6;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.COAL};
    purchaseCost = 31;
    activateCost = 3;
    numCities = 6;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.OIL};
    purchaseCost = 32;
    activateCost = 3;
    numCities = 6;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.RENEWABLE};
    purchaseCost = 33;
    activateCost = 0;
    numCities = 4;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.NUKE};
    purchaseCost = 34;
    activateCost = 1;
    numCities = 5;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.OIL};
    purchaseCost = 35;
    activateCost = 1;
    numCities = 5;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.COAL};
    purchaseCost = 36;
    activateCost = 3;
    numCities = 7;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.RENEWABLE};
    purchaseCost = 37;
    activateCost = 0;
    numCities = 4;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.TRASH};
    purchaseCost = 38;
    activateCost = 3;
    numCities = 6;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.NUKE};
    purchaseCost = 39;
    activateCost = 1;
    numCities = 5;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.OIL};
    purchaseCost = 40;
    activateCost = 2;
    numCities = 6;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.COAL};
    purchaseCost = 42;
    activateCost = 2;
    numCities = 6;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.RENEWABLE};
    purchaseCost = 44;
    activateCost = 0;
    numCities = 5;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.COAL, Resource.OIL};
    purchaseCost = 46;
    activateCost = 3;
    numCities = 7;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
    res = new Resource[] {Resource.RENEWABLE};
    purchaseCost = 50;
    activateCost = 0;
    numCities = 6;
    plantDeck.add(new PowerPlant(this, res, activateCost, numCities, purchaseCost));
    
  }
  
  public void createCities() {
    cities = new ArrayList<City>();
    cities.add(new City("Seattle")); //0
    cities.add(new City("Portland")); //1
    cities.add(new City("San Francisco")); //2
    cities.add(new City("Los Angeles")); //3
    cities.add(new City("San Diego")); //4
    cities.add(new City("Boise")); //5
    cities.add(new City("Las Vegas")); //6
    cities.add(new City("Salt Lake City")); //7
    cities.add(new City("Phoenix")); //8
    cities.add(new City("Billings")); //9
    cities.add(new City("Cheyenne")); //10
    cities.add(new City("Santa Fe")); //11
    cities.add(new City("Fargo")); //12
    cities.add(new City("Duluth")); //13
    cities.add(new City("Minneapolis")); //14
    cities.add(new City("Omaha")); //15
    cities.add(new City("Kansas City")); //16
    cities.add(new City("Oklahoma City")); //17
    cities.add(new City("Dallas")); //18
    cities.add(new City("Houston")); //19
    cities.add(new City("Chicago")); //20
    cities.add(new City("St. Louis")); //21
    cities.add(new City("Memphis")); //22
    cities.add(new City("New Orleans")); //21
    cities.add(new City("Detroit")); //24
    cities.add(new City("Cincinnati")); //25
    cities.add(new City("Knoxville")); //26
    cities.add(new City("Atlanta")); //27
    cities.add(new City("Birmingham")); //28
    cities.add(new City("Buffalo")); //29
    cities.add(new City("Pittsburgh")); //30
    cities.add(new City("Raleigh")); //31
    cities.add(new City("Savannah")); //32
    cities.add(new City("Jacksonville")); //33
    cities.add(new City("Tampa")); //34
    cities.add(new City("Miami")); //35
    cities.add(new City("Boston")); //36
    cities.add(new City("New York")); //37
    cities.add(new City("Philadelphia")); //38
    cities.add(new City("Washington")); //39
    cities.add(new City("Norfolk")); //40
    cities.add(new City("Denver")); //41 out of order cuz i forgot it
    
  }
  
  public void createCityConnections() {
    cityConnections = new ArrayList<CityConnection>();
    int city1, city2;
    int cost;
    //Seattle, Portland
    city1 = 0;
    city2 = 1;
    cost = 3;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Seattle, Billings
    city1 = 0;
    city2 = 9;
    cost = 9;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Seattle, Boise
    city1 = 0;
    city2 = 5;
    cost = 12;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Portland, Boise
    city1 = 1;
    city2 = 5;
    cost = 13;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Portland, San Francisco
    city1 = 1;
    city2 = 2;
    cost = 24;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //San Francisco, Las Vegas
    city1 = 2;
    city2 = 6;
    cost = 14;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //San Francisco, Los Angeles
    city1 = 2;
    city2 = 3;
    cost = 9;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //San Francisco, Boise
    city1 = 2;
    city2 = 5;
    cost = 23;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //San Francisco, Salt Lake City
    city1 = 2;
    city2 = 7;
    cost = 27;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Los Angeles, Las Vegas
    city1 = 3;
    city2 = 6;
    cost = 9;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Los Angeles, San Diego
    city1 = 2;
    city2 = 4;
    cost = 3;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //San Diego, Las Vegas
    city1 = 4;
    city2 = 6;
    cost = 9;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //San Diego, Phoenix
    city1 = 4;
    city2 = 8;
    cost = 14;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Boise, Billings
    city1 = 5;
    city2 = 9;
    cost = 12;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Boise, Cheyenne
    city1 = 5;
    city2 = 10;
    cost = 24;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Boise, Salt Lake City
    city1 = 5;
    city2 = 7;
    cost = 8;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Las Vegas, Salt Lake City
    city1 = 6;
    city2 = 7;
    cost = 18;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Las Vegas, Santa Fe
    city1 = 6;
    city2 = 11;
    cost = 27;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Las Vegas, Phoenix
    city1 = 6;
    city2 = 8;
    cost = 15;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Salt Lake City, Denver
    city1 = 7;
    city2 = 41;
    cost = 21;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Salt Lake City, Santa Fe
    city1 = 7;
    city2 = 11;
    cost = 28;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Phoenix, Santa Fe
    city1 = 8;
    city2 = 11;
    cost = 21;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Billings, Fargo
    city1 = 9;
    city2 = 12;
    cost = 17;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Billings, Minneapolis
    city1 = 9;
    city2 = 14;
    cost = 18;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Billings, Cheyenne
    city1 = 9;
    city2 = 10;
    cost = 9;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Cheyenne, Minneapolis
    city1 = 10;
    city2 = 14;
    cost = 18;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Cheyenne, Omaha
    city1 = 10;
    city2 = 15;
    cost = 14;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Cheyenne, Denver
    city1 = 10;
    city2 = 41;
    cost = 0;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Denver, Kansas City
    city1 = 41;
    city2 = 16;
    cost = 16;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Denver, Santa Fe
    city1 = 41;
    city2 = 11;
    cost = 13;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Santa Fe, Kansas City
    city1 = 11;
    city2 = 16;
    cost = 16;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Santa Fe, Oklahoma City
    city1 = 11;
    city2 = 17;
    cost = 15;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Santa Fe, Dallas
    city1 = 11;
    city2 = 18;
    cost = 16;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Santa Fe, Houston
    city1 = 11;
    city2 = 19;
    cost = 21;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Fargo, Duluth
    city1 = 12;
    city2 = 13;
    cost = 6;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Fargo, Minneapolis
    city1 = 12;
    city2 = 14;
    cost = 6;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Duluth, Detroit
    city1 = 13;
    city2 = 24;
    cost = 15;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Duluth, Chicago
    city1 = 13;
    city2 = 20;
    cost = 12;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Duluth, Minneapolis
    city1 = 13;
    city2 = 14;
    cost = 5;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Minneapolis, Chicago
    city1 = 14;
    city2 = 20;
    cost = 8;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Minneapolis, Omaha
    city1 = 14;
    city2 = 15;
    cost = 8;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Omaha, Chicago
    city1 = 15;
    city2 = 20;
    cost = 13;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Omaha, Kansas City
    city1 = 15;
    city2 = 16;
    cost = 5;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Kansas City, Chicago
    city1 = 16;
    city2 = 20;
    cost = 8;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Kansas City, St. Louis
    city1 = 16;
    city2 = 21;
    cost = 6;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Kansas City, Memphis
    city1 = 16;
    city2 = 22;
    cost = 12;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Kansas City, Oklahoma City
    city1 = 16;
    city2 = 17;
    cost = 8;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Oklahoma City, Memphis
    city1 = 17;
    city2 = 22;
    cost = 14;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Oklahoma City, Dallas
    city1 = 17;
    city2 = 18;
    cost = 3;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Dallas, Memphis
    city1 = 18;
    city2 = 22;
    cost = 12;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Dallas, New Orleans
    city1 = 18;
    city2 = 23;
    cost = 12;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Dallas, Houston
    city1 = 18;
    city2 = 19;
    cost = 5;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Houston, New Orleans
    city1 = 19;
    city2 = 23;
    cost = 8;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Chicago, Detroit
    city1 = 20;
    city2 = 24;
    cost = 7;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Chicago, Cincinnati
    city1 = 20;
    city2 = 25;
    cost = 7;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Chicago, St. Louis
    city1 = 20;
    city2 = 21;
    cost = 10;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //St. Louis, Cincinnati
    city1 = 21;
    city2 = 25;
    cost = 12;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //St. Louis, Atlanta
    city1 = 21;
    city2 = 27;
    cost = 12;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //St. Louis, Memphis
    city1 = 21;
    city2 = 22;
    cost = 7;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Memphis, Birmingham
    city1 = 22;
    city2 = 28;
    cost = 6;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Memphis, New Orleans
    city1 = 22;
    city2 = 23;
    cost = 7;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //New Orleans, Birmingham
    city1 = 23;
    city2 = 28;
    cost = 11;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //New Orleans, Jacksonville
    city1 = 23;
    city2 = 33;
    cost = 16;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Detroit, Buffalo
    city1 = 24;
    city2 = 29;
    cost = 7;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Detroit, Pittsburgh
    city1 = 24;
    city2 = 30;
    cost = 6;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Detroit, Cincinnati
    city1 = 24;
    city2 = 25;
    cost = 4;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Cincinnati, Pittsburgh
    city1 = 25;
    city2 = 30;
    cost = 7;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Cincinnati, Raleigh
    city1 = 25;
    city2 = 31;
    cost = 15;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Cincinnati, Knoxville
    city1 = 25;
    city2 = 26;
    cost = 6;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Knoxville, Atlanta
    city1 = 26;
    city2 = 27;
    cost = 5;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Atlanta, Raleigh
    city1 = 27;
    city2 = 31;
    cost = 7;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Atlanta, Savannah
    city1 = 27;
    city2 = 32;
    cost = 7;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Atlanta, Birmingham
    city1 = 27;
    city2 = 28;
    cost = 3;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Birmingham, Jacksonville
    city1 = 28;
    city2 = 33;
    cost = 9;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Buffalo, New York
    city1 = 29;
    city2 = 37;
    cost = 8;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Buffalo, Pittsburgh
    city1 = 29;
    city2 = 30;
    cost = 7;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Pittsburgh, Washington
    city1 = 30;
    city2 = 39;
    cost = 6;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Pittsburgh, Raleigh
    city1 = 30;
    city2 = 31;
    cost = 7;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Raleigh, Norfolk
    city1 = 31;
    city2 = 40;
    cost = 3;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Raleigh, Savannah
    city1 = 31;
    city2 = 32;
    cost = 7;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Savannah, Jacksonville
    city1 = 32;
    city2 = 33;
    cost = 0;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Jacksonville, Tampa
    city1 = 33;
    city2 = 34;
    cost = 4;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Tampa, Miami
    city1 = 34;
    city2 = 35;
    cost = 4;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Boston, New York
    city1 = 36;
    city2 = 37;
    cost = 3;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //New York, Philadelphia
    city1 = 37;
    city2 = 38;
    cost = 0;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Philadelphia, Washington
    city1 = 38;
    city2 = 39;
    cost = 3;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    //Wahington, Norfolk
    city1 = 39;
    city2 = 40;
    cost = 5;
    cityConnections.add(new CityConnection(new City[] {cities.get(city1), cities.get(city2)},cost));
    
  }

  public void sortPlayerTurnOrder() {
    int highCities = -1;
    ArrayList<Player> playersAtHighValue = new ArrayList<Player>();
    int highPlantValue = -1;
    int currNumCities;
    int currHighPlantValue;
    //potential error here, don't know if this copying method works
    ArrayList<Player> tempPlay = (ArrayList<Player>) this.players.clone();
    for(int sortInd = 0; sortInd < players.size(); sortInd++) {
      playersAtHighValue = new ArrayList<Player>();
      highCities = -1;
      for(int currPlayerInd = 0; currPlayerInd < tempPlay.size(); currPlayerInd++) {
        currNumCities = tempPlay.get(currPlayerInd).getCities().size();
        if(currNumCities > highCities) {
          highCities = currNumCities;
          playersAtHighValue = new ArrayList<Player>();
          playersAtHighValue.add(tempPlay.get(currPlayerInd));
        }
        else if(currNumCities == highCities) {
          playersAtHighValue.add(tempPlay.get(currPlayerInd));
        }
      }
      if(playersAtHighValue.size() > 1) {
        highPlantValue = -1;
        for(int i = 0; i < playersAtHighValue.size(); i++) {
          currHighPlantValue = playersAtHighValue.get(i).getHighestValuePlant().getPurchaseCost();
          if(currHighPlantValue > highPlantValue) {
            highPlantValue = currHighPlantValue;
          }
          else {
            playersAtHighValue.remove(i);
          }
        }
      }
      playersInTurnOrder.set(sortInd, playersAtHighValue.get(0));
      tempPlay.remove(playersAtHighValue.get(0));
    }
  }
  
  public boolean areCitiesConnected(City c1, City c2) {
    for (int i = 0; i < cityConnections.size(); i++) {
      if(cityConnections.get(i).contains(c1) && cityConnections.get(i).contains(c2)) {
        return true;
      }
    }
    return false;
  }
  
  public int getCityConnectionCost(City c1, City c2) {
    for (int i = 0; i < cityConnections.size(); i++) {
      if(cityConnections.get(i).contains(c1) && cityConnections.get(i).contains(c2)) {
        return cityConnections.get(i).getCost();
      }
    }
    return -1;
  }

  public ArrayList<Player> getPlayers() {
    return players;
  }

  public ArrayList<Player> getPlayersInTurnOrder() {
    return playersInTurnOrder;
  }

  public ArrayList<PowerPlant> getPlantDeck() {
    return plantDeck;
  }

  public ArrayList<PowerPlant> getPlantsOut() {
    return plantsOut;
  }

  public PowerPlant getPlantUpAuction() {
    return plantUpAuction;
  }

  public int getCurrBid() {
    return currBid;
  }

  public Player getCurrBidder() {
    return currBidder;
  }

  public ArrayList<Player> getPlayersInAuction() {
    return playersInAuction;
  }

  public ResourceBoard getResBoard() {
    return resBoard;
  }

  public int getCurrPhase() {
    return currPhase;
  }

  public int getTurnNumber() {
    return turnNumber;
  }

  public ArrayList<City> getCities() {
    return cities;
  }

  public ArrayList<CityConnection> getCityConnections() {
    return cityConnections;
  }

  public boolean isPhase3Hit() {
    return phase3Hit;
  }

  public int getPlantsAvailable() {
    return plantsAvailable;
  }

  public int getCitiesNeededPhase2() {
    return citiesNeededPhase2;
  }

  public int getCitiesNeededWin() {
    return citiesNeededWin;
  }

  public Player getWinner() {
    return winner;
  }

  public boolean isGameOver() {
    return gameOver;
  }

  public int getGameStep() {
    return gameStep;
  }

  public int getPlayerTurn() {
    return playerTurn;
  }

  public int getBiddingTurn() {
    return biddingTurn;
  }

  public boolean isSendable() {
    return sendable;
  }
  
  
  
  
  
}
