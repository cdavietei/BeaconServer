package mongoDB;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.DeleteOneModel;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.WriteModel;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lt;
import static com.mongodb.client.model.Filters.lte;
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Sorts.descending;



class DataInterface {
  public MongoClient mongoClient;
  public MongoDatabase db;
  public MongoCollection<Document> users;
  public MongoCollection<Document> beacons;

 /*
  public static void main(String[] args) {
    DataInterface DI = new DataInterface("","");

    String[] interests = {"fishing", "coding", "eating", "sleeping"};
    String id1 = DI.newUser("Taylor", "", interests);
    String id2 = DI.newUser("Chris", "password", interests);

    DI.newBeacon(id1, "hello", 0, 0, 0, 0, "Ushaia", interests);
    DI.newBeacon(id2, "none", 0, 0, 0, 0, "nowhere", interests);

    System.out.println(DI.findBeacons(2));

    System.out.println(DI.getUserByID(id1));
    //System.out.println(DI.getUserByID(id2));

  }*/


  public DataInterface(String host, String dbName) {
    mongoClient = new MongoClient();
    db = mongoClient.getDatabase("test3");
    users = db.getCollection("users");
    beacons = db.getCollection("beacons");
  }

  // User methods

  // returns String containing randomly generated ObjectID
  public String newUser(String uName, String passwd, String[] userInterests) {
	ObjectId id = new ObjectId();
	String parseID = id.toString();
	String interests = "";
	for (String str : userInterests) {
		interests = interests + str + ", ";
	}
    Document newUser = new Document("_id", id)
    				   .append("uid", parseID)
    				   .append("name", uName)
                       .append("password", passwd)
                       .append("interests", interests);
    users.insertOne(newUser);
    return parseID;

  }

  // returns JSON String representation of user object
  // returns null for not found
  public String getUserByID(String userID) {
	System.out.println(userID);
    FindIterable<Document> user = users.find(eq("uid", userID));
    System.out.println(user.first().toJson());//user.first().toJson();
    return "";
  }

  // Beacon methods

  // creates a new beacon
  public void newBeacon(String uid, String title, double latCoord, double longCoord,
		  			double duration, double range, String location, String[] userInterests) {
    String interests = "";
	for (String str : userInterests) {
		interests = interests + str + ", ";
	}
	interests = interests.substring(0,interests.length()-2);
	Document newBeacon = new Document("user_id", uid)
						 .append("title", title)
						 .append("latCoord", latCoord)
						 .append("longCoord", longCoord)
						 .append("duration", duration)
						 .append("range", range)
						 .append("location", location)
						 .append("interests", interests);
	beacons.insertOne(newBeacon);
  }

  // returns a list of n beacons in JSON format
  public String findBeacons(int n) {
	  String beaconList = "";
	  for (int i = 0; i < n; i++) {
		  FindIterable<Document> iterable = beacons.find();
		  Document beacon = iterable.first();
		  beaconList = beaconList + beacon.toJson() + ", ";
	  }
	  beaconList = beaconList.substring(0, beaconList.length()-2);
	  String jsonBeaconList = "{ \"beacon\": [" + beaconList + "]}";
	  return jsonBeaconList;
  }
}
