package beacon;

import static com.mongodb.client.model.Aggregates.limit;
import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.geoWithinCenterSphere;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Updates.addEachToSet;
import static com.mongodb.client.model.Updates.addToSet;
import static com.mongodb.client.model.Updates.pull;
import static com.mongodb.client.model.Updates.pullAll;
import static com.mongodb.client.model.Updates.set;
import static java.util.Arrays.asList;

import com.mongodb.MongoClient;
import com.mongodb.MongoWriteException;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;


public class BeaconUser {
  // database connection members
  public MongoClient mongoClient;
  public MongoDatabase db;
  public MongoCollection<Document> users;
  public MongoCollection<Document> beacons;

  // BeaconUser fields
  public String username;
  private String passwordSalt;
  private String passwordHash;
  public ArrayList<String> interests;
  public Point lastLocation;

  // use when loading a user from the database
  // follow by calling getUserByName
  public BeaconUser(String host, String dbName) {
    mongoClient = new MongoClient(host);
    db = mongoClient.getDatabase(dbName);
    users = db.getCollection("users");
    beacons = db.getCollection("beacons");
  }

  // use when creating a new user
  // follow by calling insert
  public BeaconUser(String host, String dbName, String name, String pword,
                    ArrayList<String> userInterests, double latCoord, double longCoord) {
    // connect instance to the database
    mongoClient = new MongoClient(host);
    db = mongoClient.getDatabase(dbName);
    users = db.getCollection("users");

    // initialize the fields for the user object
    username = name;
    // ***** handle password hashing ******
    interests = userInterests;
    // create a location Point: longitude first - required for database query on GeoJSON
    lastLocation = new Point(new Position(longCoord, latCoord));
  }

  // inserts the new user instance into MongoDB
  // returns true on successful insert
  // returns false on unsuccessful insert (caused by non-unique username)
  public boolean insert() {
    boolean inserted = true;

    Document user = new Document("username", this.username)
                    .append("passwordSalt", this.passwordSalt)
                    .append("passwordHash", this.passwordHash)
                    .append("interests", this.interests) // ArrayList converts to JSON array
                    .append("lastLocation", this.lastLocation); // Point coverts to GeoJSON

    try {
      users.insertOne(user);
    } catch (MongoWriteException mwe) {
      // inserted set to false on exception for non-unique username
      inserted = false;
    }

    return inserted;
  }

  // loads user instance from database into constructed BeaconUser instance
  // returns String of the entire BeaconUser object as JSON
  // returns empty String if user is not found
  public String getUserByName(String name) {
    String userAsJson = null;
    FindIterable<Document> userIterable = users.find(eq("username", name));
    Document thisUser = userIterable.first();

    if (thisUser != null) {
      this.username = thisUser.getString("username");
      this.passwordSalt = thisUser.getString("passwordSalt");
      this.passwordHash = thisUser.getString("passwordHash");
      this.interests = thisUser.get("interests", ArrayList.class); // casts interests field to ArrayList
      this.lastLocation = thisUser.get("lastLocation", Point.class);

      userAsJson = thisUser.toJson();
    }
    return userAsJson;
  }

  // Data member accessor methods

  public String getUserName() {
    return this.username;
  }

  public ArrayList<String> getInterests() {
    return this.interests;
  }

  public Point getLastLocation() {
    return this.lastLocation;
  }

  // Data member mutator methods

  public boolean changeUsername(String newName) {
    boolean updated = true;

    try {
      users.updateOne(eq("username", this.username), set("username", newName));
    } catch (MongoWriteException mwe) {
      // updated set to false on exception for non-unique username
      updated = false;
    }

    // if the update was successful, update the name in the current object
    if (updated) {
      this.username = newName;
    }

    return updated;
  }

  public boolean addInterest(String newInterest) {
    UpdateResult ur = users.updateOne(eq("username", this.username), addToSet("interests", newInterest));
    return (ur.getModifiedCount() > 0);
  }

  public boolean addInterests(ArrayList<String> newInterests) {
    UpdateResult ur = users.updateOne(eq("username", this.username), addEachToSet("interests", newInterests));
    return (ur.getModifiedCount() > 0);
  }

  public boolean removeInterest(String removeTarget) {
    UpdateResult ur = users.updateOne(eq("username", this.username), pull("interests", removeTarget));
    return (ur.getModifiedCount() > 0);
  }

  public boolean removeInterests(ArrayList<String> removeTargets) {
    UpdateResult ur = users.updateOne(eq("username", this.username), pullAll("interests", removeTargets));
    return (ur.getModifiedCount() > 0);
  }

  public boolean updateLastLocation(double latCoord, double longCoord) {
    Point newLocation = new Point(new Position(longCoord, latCoord));
    UpdateResult ur = users.updateOne(eq("username", this.username), set("lastLocation", newLocation));
    return (ur.getModifiedCount() > 0);
  }

  // Beacon search methods

  // input max number of beacons, user's coordinates, and proximity in miles
  // returns JSON formatted String of the form { beacons: [ <beacons> ]}
  // where <beacons> is a list of max beacons within distance of the user's coordinates
  public String findNearbyBeacons(int max, double latCoord, double longCoord, double distance) {
    // filter by the geoWithinCenterSphere filter
    // distance / 3963.2 converts distance to radians (3963.2 approximates Earth's radius)
    FindIterable<Document> iterable = beacons.find(geoWithinCenterSphere(
                                                    "location",
                                                    longCoord,
                                                    latCoord,
                                                    distance / 3963.2
                                                   ))
                                                   .limit(max);

    String result = iterableToJson("beacons", iterable);
    return result;
  }

  public String findNearbyBeaconsByTags(int max, double latCoord, double longCoord, double distance, ArrayList<String> tags) {
    // aggregate result Documents from beacons collection
    AggregateIterable<Document> beaconAggregation = beacons.aggregate(asList(
      // first match nearby beacons
      match(geoWithinCenterSphere("location", longCoord, latCoord, distance / 3963.2)),
      // only match beacons with the correct tags
      match(in("tags", tags)),
      // limit the aggregation to specified number of beacons
      limit(max)
    ));

    String result = iterableToJson("beacons", beaconAggregation);
    return result;
  }

  // returns a JSON formatted String in the form { <name> : [<content>] }
  // where <content> is the list of Documents in the iterable parameter also in JSON format
  public static String iterableToJson(String name, MongoIterable<Document> iterable) {
    String returnList = "{ \"" + name + "\": [";

    // get the response in the iterable as a List of Documents
    List<Document> docList = iterable.into(new ArrayList<Document>());

    if (!docList.isEmpty()) {
      for (Document doc: docList) {
        returnList += doc.toJson() + ",";
      }

      // remove the final, extra comma by taking substring of everything but last character
      returnList = returnList.substring(0, returnList.length() - 2);
    }
    returnList += "]}"; // complete correct JSON format

    return returnList;
  }

  // close the user's connection to the database
  public void closeConnection() {
    mongoClient.close();
  }
}
