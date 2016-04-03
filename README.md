# BeaconServer

The RESTful endpoints of the Beacon API allow you to:


Create and Update Users:

https://ec2-52-90-59-17.compute-1.amazonaws.com/UpdateUser?uid=<username>&lat=<latitude>&long=<longitude>

If <username> has never been created as a user, a new user will be created with that username
as well as the specified latitude and longitude. If the user has already been created,
their latitude and longitude are updated to the specified coordinates.


Place a Beacon:

https://ec2-52-90-59-17.compute-1.amazonaws.com/CreateBeacon?uid=<username>&lat=<latitude>&long=<longitude>&range=<range>&title=<title>&start=<start time>&end=<end time>&tags=<list of tags>

This creates a new Beacon with <username> as its creator. The Beacon is located at the specified coordinates and the range is specified in miles. The title and tags explain what it is, and the start and end times are specified as Unix time (seconds since January 1st, 1970).


Find Nearby Beacons:

https://ec2-52-90-59-17.compute-1.amazonaws.com/NearbyBeacons?lat=<latitude>&long=<longitude>&dist=<distance>&max=<maximum>

You specify the <maximum> number of Beacons you would like to see. Up to that many Beacons are returned as long as they are within <distance> miles of the specified <latitude> and <longitude> coordinates.
