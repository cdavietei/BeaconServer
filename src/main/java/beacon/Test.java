package beacon;

import java.util.ArrayList;

public class Test {
  public static void main(String[] args) {
    BeaconUser user1 = new BeaconUser("localhost", "test", "Taylor", "", new ArrayList<String>(), 42.2, 71.1);
    user1.insert();
    BeaconUser user2 = new BeaconUser("localhost", "test");
    System.out.println(user2.getUserByName("Taylor"));
  }
}
