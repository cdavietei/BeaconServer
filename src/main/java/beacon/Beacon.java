package beacon;

import static com.mongodb.client.model.Aggregates.limit;
import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gt;
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

  // use when creating a new beacon
  // follow by calling insert
  public Beacon(MongoClient mg, MongoDatabase mdb, String authorName, String beaconTitle, double latCoord, double longCoord,
                Date start, Date end, double beaconRange, String beaconAddress, ArrayList<String> tagList) {
    // connect instance to database
    mongoClient = mg;
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
}
