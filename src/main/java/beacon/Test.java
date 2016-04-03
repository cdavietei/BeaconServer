package beacon;

import java.util.ArrayList;
import java.util.Date;

public class Test {
  public static void main(String[] args) {
    BeaconUser user1 = new BeaconUser("localhost", "test", "Taylor", "password", new ArrayList<String>(), 42.2, 71.1);
    user1.insert();
    if (user1.authenticate("hello")) {
      if(user1.placeBeacon("newBeacon", 42.2, 71.1, new Date(143143143), new Date(431431431), 10.0, "myHouse", "myAddress", new ArrayList<String>())){
      System.out.println("Beacon placed");}
    } else {
      System.out.println("wrong password, dummy");
    }

  }
}
