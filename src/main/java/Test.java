package beacon;

public class Test {
  public static void main(String[] args) {
    BeaconUser user = new BeaconUser("localhost", "test");
    System.out.println(user.getUserByName("Taylor"));
  }
}
