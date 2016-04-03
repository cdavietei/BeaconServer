package beacon;

import com.mongodb.MongoClient;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
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

  // use when loading a beacon from the database
  public Beacon(String authorName, String beaconTitle, double latCoord, double longCoord, Date start,
                Date end, double dist, String address, ArrayList<String> tagList) {
    // construct the GeoJSON for the location field
    Point loc = new Point(new Position(longCoord, latCoord));
  }
}
