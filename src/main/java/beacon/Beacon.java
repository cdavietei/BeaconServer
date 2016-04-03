package beacon;

import static com.mongodb.client.model.Aggregates.limit;
import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.geoWithinCenterSphere;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.lt;
import static com.mongodb.client.model.Filters.nin;
import static java.util.Arrays.asList;

import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Date;

public class Beacon {
  // database connection members
  public MongoClient mongoClient;
  public MongoDatabase db;
  public MongoCollection<Document> users;
  public MongoCollection<Document> beacons;

  // beacon data members
  String creator;
  String title;
  Point location;
  Date startTime;
  Date endTime;
  double range;
  String address;
  ArrayList<String> tags;
  int notifiedCount;
  ArrayList<String> notifiedUsers;

  // empty constructor, creates database connection
  // follow with findUniqueBeacon
  public Beacon(MongoClient mc, MongoDatabase mdb) {
    // connect instance to database
    mongoClient = mc;
    db = mdb;
    users = db.getCollection("users");
    beacons = db.getCollection("beacons");
  }

  // use when creating a new beacon
  // follow by calling insert
  public Beacon(MongoClient mc, MongoDatabase mdb, String authorName, String beaconTitle, double latCoord, double longCoord,
                Date start, Date end, double beaconRange, String beaconAddress, ArrayList<String> tagList) {
    // connect instance to database
    mongoClient = mc;
    db = mdb;
    users = db.getCollection("users");
    beacons = db.getCollection("beacons");

    // initialize the fields for the beacon object
    creator = authorName;
    title = beaconTitle;
    location = new Point(new Position(longCoord, latCoord));
    startTime = start;
    endTime = end;
    range = beaconRange;
    address = beaconAddress;
    notifiedCount = 1; // creator is considered first notified
    ArrayList<String> nu = new ArrayList<String>();
    nu.add(creator);
    notifiedUsers = nu;
  }

  // loads a unique beacon into the current Beacon instance
  //

  // inserts the new user instance into MongoDB
  // returns true on successful insertion
  // returns false on unsuccessful (unsuccessful if user already has a beacon placed)
  public boolean insert() {
    boolean insert = true;

    // search for a beacon by the creator with an endTime greater than current time
    // limits users to one beacon at a time
    FindIterable<Document> fi = beacons.find(and(asList(
      eq("creator", this.creator),
      gt("endTime", new Date())
    )))
    .limit(1);

    // do not insert if a match is found
    if (fi.first() == null) {
      insert = false;
    }

    if (insert) {
      Document beacon = new Document("creator", this.creator)
                        .append("title", this.title)
                        .append("location", this.location)
                        .append("startTime", this.startTime)
                        .append("endTime", this.endTime)
                        .append("range", this.range)
                        .append("address", this.address)
                        .append("notifiedCount", this.notifiedCount)
                        .append("notified", this.notifiedUsers);

      beacons.insertOne(beacon);
    }

    return insert;
  }

  // remove the user's current beacon
  // returns true for successful deletion
  public boolean deleteCurrentBeacon() {
    // delete the beacon by the creator with an endTime greater than current time
    DeleteResult dr = beacons.deleteOne(and(asList(
      eq("creator", this.creator),
      gt("endTime", new Date())
    )));

    return (dr.getDeletedCount() > 0);
  }

  // loads the beacon with the specified creator at the specified time
  // the time can be any time within the duration of the beacon
  // returns true if a beacon is found, false if not
  public boolean findBeacon(String beaconCreator, Date dateTime) {
    boolean found = false;

    FindIterable<Document> fi = beacons.find(and(asList(
      eq("creator", this.creator),
      gt("endTime", dateTime),
      lt("startTime", dateTime)
    )))
    .limit(1);

    Document thisBeacon = fi.first();

    // if found, load the information into the Beacon instance
    if (thisBeacon != null) {
      found = true;

      this.creator = thisBeacon.getString("creator");
      this.title = thisBeacon.getString("title");
      // parse location document to get coordinates
      Document loc = (Document) thisBeacon.get("location");
      ArrayList<Double> coords = loc.get("coordinates", ArrayList.class);
      this.location = new Point(new Position(coords.get(0), coords.get(1)));
      this.startTime = thisBeacon.get("startTime", Date.class);
      this.endTime = thisBeacon.get("endTime", Date.class);
      this.range = thisBeacon.getDouble("range");
      this.address = thisBeacon.getString("address");
      this.notifiedCount = thisBeacon.getInteger("notifiedCount");
      this.notifiedUsers = thisBeacon.get("notified", ArrayList.class);
    }

    return found;

  }

  // input beacon's coordinates, proximity in miles, and list of users that have already attended
  // returns JSON formatted String of the form { users: [ <users> ]}
  // where <users> is a list of users within range of the beacon that have not yet been notified
  public String findNearbyUsers() {
    // aggregate result Documents from users collection
    AggregateIterable<Document> userAggregation = users.aggregate(asList(
        // first aggregate by finding users within range of the beacon
        match(geoWithinCenterSphere( "lastLocation",
                                     this.longCoord,
                                     this.latCoord,
                                     this.range / 3963.2 )),
        // only match users that are not in the previously notified list
        match(nin( "username", this.notifiedUsers ))
    ));

    String result = JsonHelpers.iterableToJson("users", userAggregation);
    return result;
  }

  // private version of the nearbyUsers method
  // input is the same as findNearbyUsers with the addition of selected ArrayList
  // selected is a list of friends selected to receive notification about the beacon
  // returns JSON formatted String of the form { users: [ <users> ]}
  // where <users> is a list of users within range of the beacon that have not yet been notified
  public String privateFindNearbyUsers(ArrayList<String> selected) {
    // aggregate result Documents from users collection
    AggregateIterable<Document> userAggregation = users.aggregate(asList(
      // first match on the users specified in the selected list
      match(in( "username", selected )),
      // then find those close to beacon
      match(geoWithinCenterSphere( "lastLocation", this.longCoord, this.latCoord, this.range / 3963.2)),
      // only match users that have not yet been notified
      match(nin( "username", this.notifiedUsers ))
    ));

    String result = iterableToJson("users", userAggregation);
    return result;
  }
}
