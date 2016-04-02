package beacon;

import com.mongodb.client.MongoIterable;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

class JsonHelpers {
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
}
