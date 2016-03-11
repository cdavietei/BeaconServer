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


  public static void main(String[] args) {
    DataInterface DI = new DataInterface("","");
    String[] interests = {"fishing", "coding", "eating", "sleeping"};
    String id1 = DI.newUser("Taylor", "", interests);
    String id2 = DI.newUser("Chris", "password", interests);
    System.out.println(DI.getUserByID(id1));
    System.out.println(DI.getUserByID(id2));
  }


  public DataInterface(String host, String dbName) {
    mongoClient = new MongoClient();
    db = mongoClient.getDatabase("test");
    users = db.getCollection("users");
    beacons = db.getCollection("beacons");
  }

  // returns String containing randomly generated ObjectID
  public String newUser(String uName, String passwd, String[] userInterests) {
	ObjectId id = new ObjectId();
	String interests = "";
	for (String str : userInterests) {
		interests = interests + str + ", ";
	}
    Document newUser = new Document("_id", id)
    				   .append("name", uName)
                       .append("password", passwd)
                       .append("interests", interests);
    users.insertOne(newUser);
    return id.toString();

  }

  // returns JSON String representation of user object
  // returns null for not found
  public String getUserByID(String userID) {
    FindIterable<Document> user = users.find(eq("_id", userID));
    return user.first().toJson();
  }

  public void newBeacon(String uid, String title, double latCoord, double longCoord,
		  			double duration, double range, String location, String[] userInterests) {
    String interests = "";
	for (String str : userInterests) {
		interests = interests + str + ", ";
	}
	Document newBeacon = new Document("user_id", uid)
						 .append("title", title)
						 .append("latCoord", latCoord)
						 .append("longCoord", longCoord)
						 .append("duration", duration)
						 .append("range", range)
						 .append("location", location)
						 .append("interests", interests);
  }
}
