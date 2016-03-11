import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;


class dataInterface {
  public MongoClient mongoClient;
  public MongoDatabase db;
  public MongoCollection<Document> users;
  public MongoCollection<Documents> beacons;

 /*
  public static void main(String[] args) {
    dataInterface DI = new dataInterface(args[0], args[1]);
    String[] interests = {"fishing", "coding", "eating", "sleeping"};
    DI.insertUser("Taylor", "", interests);
    DI.insertUser("Chris", "password", interests);
    System.out.println(DI.getUserByName("Taylor"));
  }
*/

  public dataInterface(String host, String dbName) {
    mongoClient = new MongoClient(host);
    db = mongoClient.getDatabase(dbName);
    users = db.getCollection("users");
    beacons = db.getCollection("beacons");
  }

  // returns String containing randomly generated ObjectID
  public String newUser(String uName, String passwd, String[] userInterests) {
    Document newUser = new Document("name", uName)
                       .append("password", passwd)
                       .append("interests", userInterests);
    WriteResult wres = users.insertOne(newUser);
    return wres._id.valueOf();
  }

  // returns JSON String representation of user object
  // returns null for not found
  public String getUserByName(String nameQuery) {
    MongoCursor<Document> user = users.find(eq("name", nameQuery));
    if (user.hasNext()) {
      return user.next().toJson();
    } else {
      return null;
    }
  }

  // returns JSON String representation of user object
  // returns null for not found
  public String getUserByID(int userID) {
    MongoCursor<Document> user = users.find(eq("_id", userID));
    if (user.hasNext()) {
      return user.next().toJson();
    } else {
      return null;
    }
  }


}
