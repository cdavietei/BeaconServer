package beacon;

import static com.mongodb.client.model.Aggregates.limit;
import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.geoWithinCenterSphere;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Filters.lt;
import static com.mongodb.client.model.Filters.nin;
import static com.mongodb.client.model.Updates.addEachToSet;
import static com.mongodb.client.model.Updates.addToSet;
import static com.mongodb.client.model.Updates.pull;
import static com.mongodb.client.model.Updates.pullAll;
import static com.mongodb.client.model.Updates.set;
import static java.util.Arrays.asList;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.MongoWriteException;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Beacon {
  // database connection members
  public MongoDatabase db;
  public MongoCollection<Document> users;
  public MongoCollection<Document> beacons;

  // beacon data members
  String creator;
  String title;
  Point location;
  Date startTime;
  Date endTime;
  Double range;
  String placeName;
  String address;
  ArrayList<String> tags;
  Integer notifiedCount;
  ArrayList<String> notified;

  // empty constructor, creates database connection
  // the user's db connection is passed to the beacon as mdb
  // follow with findUniqueBeacon
  public Beacon(MongoDatabase mdb) {
    // connect instance to database
    db = mdb;
    users = db.getCollection("users");
    beacons = db.getCollection("beacons");
  }

  // use when creating a new beacon
  // the user's db connection is passed to the beacon as mdb
  // follow by calling insert
  public Beacon(MongoDatabase mdb, String authorName, String beaconTitle, Double latCoord, Double longCoord,
                Date start, Date end, Double beaconRange, String pName, String beaconAddress, ArrayList<String> tagList) {
    // connect instance to database
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
    placeName = pName;
    address = beaconAddress;
    notifiedCount = 1; // creator is considered first notified
    ArrayList<String> nu = new ArrayList<String>();
    nu.add(creator);
    notified = nu;
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
    if (fi.first() != null) {
      insert = false;
    }

    if (insert) {
      Document beacon = new Document("creator", this.creator)
                        .append("title", this.title)
                        .append("location", this.location)
                        .append("startTime", this.startTime)
                        .append("endTime", this.endTime)
                        .append("range", this.range)
                        .append("placeName", this.placeName)
                        .append("address", this.address)
                        .append("notifiedCount", this.notifiedCount)
                        .append("notified", this.notified);

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
      Document loc = thisBeacon.get("location", Document.class);
      ArrayList<Double> coords = loc.get("coordinates", ArrayList.class);
      this.location = new Point(new Position(coords.get(0), coords.get(1)));
      this.startTime = thisBeacon.get("startTime", Date.class);
      this.endTime = thisBeacon.get("endTime", Date.class);
      this.range = thisBeacon.getDouble("range");
      this.placeName = thisBeacon.getString("placeName");
      this.address = thisBeacon.getString("address");
      this.notifiedCount = thisBeacon.getInteger("notifiedCount");
      this.notified = thisBeacon.get("notified", ArrayList.class);
    }

    return found;

  }

  // Data member accessor methods

  public String getCreator() {
    return this.creator;
  }

  public String getTitle() {
    return this.title;
  }

  public Point getLocation() {
    return this.location;
  }

  public Date getStartTime() {
    return this.startTime;
  }

  public Date getEndTime() {
    return this.endTime;
  }

  public Double getRange() {
    return this.range;
  }

  public String getPlaceName() {
    return this.placeName;
  }

  public String getAddress() {
    return this.address;
  }

  public ArrayList<String> getTags() {
    return this.tags;
  }

  public Integer getNotifiedCount() {
    return this.notifiedCount;
  }

  public ArrayList<String> getNotifiedUsers() {
    return this.notified;
  }

  // Data member mutator methods

  public boolean updateTitle(String newTitle) {
    return updateBeaconField(this.title, newTitle);
  }

  public boolean updateLocation(Double latCoord, Double longCoord) {
    Point newLocation = new Point(new Position(longCoord, latCoord));
    return updateBeaconField(this.location, newLocation);
  }

  public boolean updateStartTime(Date start) {
    return updateBeaconField(this.startTime, start);
  }

  public boolean updateEndTime(Date end) {
    return updateBeaconField(this.endTime, end);
  }

  public boolean updateRange(Double newRange) {
    return updateBeaconField(this.range, newRange);
  }

  public boolean updatePlaceName(String newPlaceName) {
    return updateBeaconField(this.placeName, newPlaceName);
  }

  public boolean updateAddress(String newAddress) {
    return updateBeaconField(this.address, newAddress);
  }

  public boolean addTag(String newTag) {
    UpdateResult ur = beacons.updateOne(
                                and(asList(eq("creator", this.creator),
                                           eq("endTime", this.endTime)
                                )),
                                addToSet("tags", newTag)
    );
    boolean completed = (ur.getModifiedCount() > 0);
    if (completed) {
      this.tags.add(newTag);
    }
    return completed;
  }

  public boolean addTags(ArrayList<String> newTags) {
    UpdateResult ur = beacons.updateOne(
                                and(asList(eq("creator", this.creator),
                                           eq("endTime", this.endTime)
                                )),
                                addEachToSet("tags", newTags)
    );
    boolean completed = (ur.getModifiedCount() > 0);
    if (completed) {
      this.tags.addAll(newTags);
    }
    return completed;
  }

  public boolean removeTag(String removeTarget) {
    UpdateResult ur = beacons.updateOne(
                                and(asList(eq("creator", this.creator),
                                           eq("endTime", this.endTime)
                                )),
                                pull("tags", removeTarget)
    );
    boolean completed = (ur.getModifiedCount() > 0);
    if (completed) {
      this.tags.remove(removeTarget);
    }
    return completed;
  }

  public boolean removeTags(ArrayList<String> removeTargets) {
    UpdateResult ur = beacons.updateOne(
                                and(asList(eq("creator", this.creator),
                                           eq("endTime", this.endTime)
                                )),
                                pullAll("tags", removeTargets)
    );
    boolean completed = (ur.getModifiedCount() > 0);
    if (completed) {
      this.tags.removeAll(removeTargets);
    }
    return completed;
  }

  public boolean updateNotifiedCount(Integer newCount) {
    return updateBeaconField(this.notifiedCount, newCount);
  }

  public boolean addOneNotified(String newNotified) {
    UpdateResult ur = beacons.updateOne(
                                and(asList(eq("creator", this.creator),
                                           eq("endTime", this.endTime)
                                )),
                                addToSet("notified", newNotified)
    );
    boolean completed = (ur.getModifiedCount() > 0);
    if (completed) {
      this.notified.add(newNotified);
    }
    return completed;
  }

  public boolean addManyNotified(ArrayList<String> newNotifieds) {
    UpdateResult ur = beacons.updateOne(
                                and(asList(eq("creator", this.creator),
                                           eq("endTime", this.endTime)
                                )),
                                addEachToSet("notified", newNotifieds)
    );
    boolean completed = (ur.getModifiedCount() > 0);
    if (completed) {
      this.notified.addAll(newNotifieds);
    }
    return completed;
  }

  // general method for simple beacon field updates
  public boolean updateBeaconField(Object field, Object newFieldValue) {
    boolean completed = true;
    try {
      UpdateResult ur = beacons.updateOne(
                                and(asList(eq("creator", this.creator),
                                           eq("endTime", this.endTime)
                                )),
                                set(field.toString(), newFieldValue)
      );
      completed = (ur.getModifiedCount() > 0);
    } catch (MongoWriteException mwe) {
      completed = false;
    }
    if (completed) {
      field = newFieldValue;
    }
    return completed;
  }

  // User location methods

  // input beacon's coordinates, proximity in miles, and list of users that have already attended
  // returns JSON formatted String of the form { users: [ <users> ]}
  // where <users> is a list of users within range of the beacon that have not yet been notified
  public String findNearbyUsers() {
    // get the lat and long coords from the location field
    Position coords = this.location.getCoordinates();
    List<Double> coordList = coords.getValues();
    // aggregate result Documents from users collection
    AggregateIterable<Document> userAggregation = users.aggregate(asList(
        // first aggregate by finding users within range of the beacon
        match(geoWithinCenterSphere( "lastLocation",
                                     coordList.get(0),
                                     coordList.get(1),
                                     this.range / 3963.2 )),
        // only match users that are not in the previously notified list
        match(nin( "username", this.notified ))
    ));

    String result = Helpers.iterableToJson("users", userAggregation);
    return result;
  }

  // private version of the nearbyUsers method
  // input is the same as findNearbyUsers with the addition of selected ArrayList
  // selected is a list of friends selected to receive notification about the beacon
  // returns JSON formatted String of the form { users: [ <users> ]}
  // where <users> is a list of users within range of the beacon that have not yet been notified
  public String privateFindNearbyUsers(ArrayList<String> selected) {
    // get the lat and long coords from the location field
    Position coords = this.location.getCoordinates();
    List<Double> coordList = coords.getValues();
    // aggregate result Documents from users collection
    AggregateIterable<Document> userAggregation = users.aggregate(asList(
      // first match on the users specified in the selected list
      match(in( "username", selected )),
      // then find those close to beacon
      match(geoWithinCenterSphere( "lastLocation", coordList.get(0), coordList.get(1), this.range / 3963.2)),
      // only match users that have not yet been notified
      match(nin( "username", this.notified ))
    ));

    String result = Helpers.iterableToJson("users", userAggregation);
    return result;
  }
}
