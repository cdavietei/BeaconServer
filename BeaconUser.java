package beacon;

import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;

import java.util.ArrayList;

class BeaconUser {
  String username;
  String passwordSalt;
  String passwordHash;
  ArrayList<String> interests;
  Point lastLocation;
}
