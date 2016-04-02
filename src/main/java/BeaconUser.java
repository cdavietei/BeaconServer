package beacon;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.push;
import static com.mongodb.client.model.Updates.pushEach;
import static com.mongodb.client.model.Updates.set;

import com.mongodb.MongoClient;
import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;


class BeaconUser {
  // database connection members
  public MongoClient mongoClient;
  public MongoDatabase db;
  public MongoCollection<Document> users;
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
  public String getUserByName(String name) {
    FindIterable<Document> userIterable = users.find(eq("username", name));
    Document thisUser = userIterable.first();

    this.username = thisUser.getString("username");
    this.passwordSalt = thisUser.getString("passwordSalt");
    this.passwordHash = thisUser.getString("passwordHash");
    this.interests = thisUser.get("interests", ArrayList.class); // casts interests field to ArrayList
    this.lastLocation = thisUser.get("lastLocation", Point.class);

    return thisUser.toJson();
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

  public void addInterest(String newInterest) {
    users.updateOne(eq("username", this.username), push("interests", newInterest));
  }

  public void addInterests(ArrayList<String> newInterests) {
    users.updateOne(eq("username", this.username), pushEach("interests", newInterests));
  }

  // close the user's connection to the database
  public void closeConnection() {
    mongoClient.close();
  }
}
