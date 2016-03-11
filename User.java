import java.util.ArrayList;

class User {
  public String userName;
  public int deviceId;
  public ArrayList<String> interests;

  public User(String uname, int devId, String[] inters) {
    username = uname;
    deviceId = devId;

    for (String i : inters) {
      interests.add(i);
    }
  }

  // updates the username
  public void updateUserName(String newName) {
    userName = newName;
  }

  // adds an interest to the interests ArrayList
  // returns true on successful insertion, false for failure
  public boolean addInterest(String newInterest) {
    return interests.add(newInterest);
  }

  // adds multiple interests in the form of a String array
  // returns true if all insertions were successful, false for any failure
  public boolean addInterests(String[] newInterests) {
    boolean allInserted = true;

    for (String i : newInterests) {
      allInserted = allInserted && interests.add(i);
    }

    return allInserted;
  }
}
