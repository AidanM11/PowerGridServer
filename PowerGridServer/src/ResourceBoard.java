import java.io.Serializable;
import java.util.HashMap;

public class ResourceBoard implements Serializable{
  //max for trash, oil and coal are 24, nukes are 12
  private HashMap<Resource, Integer> resourcesAvailable;
  private Gamestate gamestate;
  
  public ResourceBoard(Gamestate gamestate) {
    this.resourcesAvailable = new HashMap<Resource, Integer>();
    this.resourcesAvailable.put(Resource.COAL, 24);
    this.resourcesAvailable.put(Resource.OIL, 18);
    this.resourcesAvailable.put(Resource.NUKE, 2);
    this.resourcesAvailable.put(Resource.TRASH, 6);
    this.gamestate = gamestate;
  }
  
  //only works for USA
  public int getPriceOfResource(Resource res) {
    if(res == Resource.RENEWABLE) {
      System.out.println("tried to get price of renewable");
      return 0;
    }
    if(res == Resource.NUKE) {
      if(resourcesAvailable.get(Resource.NUKE) > 4) {
        return 13 - resourcesAvailable.get(Resource.NUKE);
      }
      else {
        return 8 + 2*(5 - resourcesAvailable.get(Resource.NUKE));
      }
    }
    int r = resourcesAvailable.get(res);
    return (int) Math.ceil(((double)(25-r))/((double)3));
  }
  
  public void refillResources(int phase, int players) {
    if(players == 3) {
      if(phase == 1) {
        changeResourceAmount(Resource.COAL, 4);
        changeResourceAmount(Resource.OIL, 2);
        changeResourceAmount(Resource.TRASH, 1);
        changeResourceAmount(Resource.NUKE, 1);
      }
      if(phase == 2) {
        changeResourceAmount(Resource.COAL, 5);
        changeResourceAmount(Resource.OIL, 3);
        changeResourceAmount(Resource.TRASH, 2);
        changeResourceAmount(Resource.NUKE, 1);
      }
      if(phase == 3) {
        changeResourceAmount(Resource.COAL, 3);
        changeResourceAmount(Resource.OIL, 4);
        changeResourceAmount(Resource.TRASH, 3);
        changeResourceAmount(Resource.NUKE, 1);
      }
    }
    if(players == 4) {
      if(phase == 1) {
        changeResourceAmount(Resource.COAL, 5);
        changeResourceAmount(Resource.OIL, 3);
        changeResourceAmount(Resource.TRASH, 2);
        changeResourceAmount(Resource.NUKE, 1);
      }
      if(phase == 2) {
        changeResourceAmount(Resource.COAL, 6);
        changeResourceAmount(Resource.OIL, 4);
        changeResourceAmount(Resource.TRASH, 3);
        changeResourceAmount(Resource.NUKE, 2);
      }
      if(phase == 3) {
        changeResourceAmount(Resource.COAL, 4);
        changeResourceAmount(Resource.OIL, 5);
        changeResourceAmount(Resource.TRASH, 4);
        changeResourceAmount(Resource.NUKE, 2);
      }
    }
    if(players == 5) {
      if(phase == 1) {
        changeResourceAmount(Resource.COAL, 5);
        changeResourceAmount(Resource.OIL, 4);
        changeResourceAmount(Resource.TRASH, 3);
        changeResourceAmount(Resource.NUKE, 2);
      }
      if(phase == 2) {
        changeResourceAmount(Resource.COAL, 7);
        changeResourceAmount(Resource.OIL, 5);
        changeResourceAmount(Resource.TRASH, 3);
        changeResourceAmount(Resource.NUKE, 3);
      }
      if(phase == 3) {
        changeResourceAmount(Resource.COAL, 5);
        changeResourceAmount(Resource.OIL, 6);
        changeResourceAmount(Resource.TRASH, 5);
        changeResourceAmount(Resource.NUKE, 2);
      }
    }
    if(players == 6) {
      if(phase == 1) {
        changeResourceAmount(Resource.COAL, 7);
        changeResourceAmount(Resource.OIL, 5);
        changeResourceAmount(Resource.TRASH, 3);
        changeResourceAmount(Resource.NUKE, 2);
      }
      if(phase == 2) {
        changeResourceAmount(Resource.COAL, 9);
        changeResourceAmount(Resource.OIL, 6);
        changeResourceAmount(Resource.TRASH, 5);
        changeResourceAmount(Resource.NUKE, 3);
      }
      if(phase == 3) {
        changeResourceAmount(Resource.COAL, 6);
        changeResourceAmount(Resource.OIL, 7);
        changeResourceAmount(Resource.TRASH, 6);
        changeResourceAmount(Resource.NUKE, 3);
      }
    }
    
    //capping resources
    if(resourcesAvailable.get(Resource.COAL) > 24) {
      resourcesAvailable.put(Resource.COAL, 24);
    }
    if(resourcesAvailable.get(Resource.OIL) > 24) {
      resourcesAvailable.put(Resource.OIL, 24);
    }
    if(resourcesAvailable.get(Resource.TRASH) > 24) {
      resourcesAvailable.put(Resource.TRASH, 24);
    }
    if(resourcesAvailable.get(Resource.NUKE) > 12) {
      resourcesAvailable.put(Resource.NUKE, 12);
    }
  }
  
  public void changeResourceAmount (Resource res, int amt) {
    this.resourcesAvailable.put(res, resourcesAvailable.get(res) + amt);
  }
  
  
  
}
