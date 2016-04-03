package beacon;

import java.util.ArrayList;

public class Test {
  public static void main(String[] args) {
    BeaconUser user1 = new BeaconUser("localhost", "test", "Taylor", "hello", new ArrayList<String>(), 42.2, 71.1);
    if (user1.authenticate("hello")) {
      user1.insert();
      System.out.println("inserted");
    } else {
      System.out.println("wrong password, dummy");
    }


  }
}
