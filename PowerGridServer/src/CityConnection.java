import java.io.Serializable;

public class CityConnection implements Serializable{
  public City[] cities;
  public int cost;
  
  public CityConnection(City[] cities, int cost) {
    super();
    this.cities = cities;
    this.cost = cost;
  }
  
  public boolean contains(City city) {
    for(int i = 0; i < cities.length; i++) {
      if(cities[i] == city) {
        return true;
      }
    }
    return false;
  }
  
  public City[] getCities() {
    return cities;
  }
  public int getCost() {
    return cost;
  }
  
  
}
