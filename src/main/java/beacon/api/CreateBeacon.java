package beacon.api;

import beacon.*;
import beacon.util.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.net.*;
import java.util.*;

import org.bson.*;

public class CreateBeacon extends HttpServlet
{
    protected static String MONGO_HOST;
    protected static String DB_NAME;
    protected BeaconUser user;

    protected String resp;

    public void init()
    {
        MONGO_HOST = Enums.MONGO_HOST.toString();
        DB_NAME = Enums.DB_NAME.toString();

        user = new BeaconUser(MONGO_HOST,DB_NAME);
    }

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
              throws ServletException, IOException
    {

        //resp = "";
        response.setContentType("text/plain");

        boolean success = false;

        String uid = request.getParameter("uid");

        Date start, end;
        start = new Date(System.currentTimeMillis());
        end = new Date(System.currentTimeMillis()+(5*1000*60*60));
        if(uid != null)
        {
            String title = request.getParameter("title");

            double latCoord = Double.parseDouble(request.getParameter("lat"));
            double longCoord = Double.parseDouble(request.getParameter("long"));
            double range = Double.parseDouble(request.getParameter("range"));

            start = new Date(Long.parseLong(request.getParameter("start"))*1000);
            end = new Date(Long.parseLong(request.getParameter("end"))*1000);

            String[] tags = request.getParameterValues("tags");
            String[] address = getAddress(latCoord,longCoord);

            success = createBeacon(uid,title, latCoord, longCoord, start, end,
                        range, address[0], address[1], tags);
        }

        PrintWriter out = response.getWriter();
        out.println(((success) ? "Success" : "Failure"));

        out.close();
    }

    public boolean createBeacon(String uid, String title, double latCoord,
                    double longCoord, Date start,
                    Date end, double range, String placeName,
                    String address, String[] tags)
    {
        boolean retval = false;

        String response = user.getUserByName(uid);

        //resp += "<br><br>"+response+"<br><br>";

        if(response == null)
        {
            user.closeConnection();
            user = new BeaconUser(MONGO_HOST, DB_NAME, uid, "",
                    new ArrayList<String>(), latCoord, longCoord);
            //resp +="<br><br>Insert: "+user.insert()+"<br><br>";
        }
        ArrayList<String> list = new ArrayList<String>(Arrays.asList(tags));
        //resp+= String.format("%s, %f, %f, %d, %d, %f, %s, %s, %s",title,
        //latCoord, longCoord, (int)start.getTime(), (int)end.getTime(), range, placeName, address, list.toString());

        retval = user.placeBeacon(title, latCoord, longCoord,start, end,
                    range, placeName, address, list);

        //resp+="<br><br>"+retval+"<br><br><br>";

        return retval;
    }

    public String[] getAddress(double latCoord, double longCoord)
    {


        String[] retval = {"NYU","251 Mercer St."};

        if(true)
            return retval;

        try
        {
            String client = "X4Z021O0Q1QNG3BRTNAZBG5WWZPHQEXYA3NZW40AHJ1JSQJM";
            String secret = "OP344JWOZIAQU5YYEDZOEBYDBEBKRJMFY1BQWUO3KC4ZEDHM";
            String base = "https://api.foursquare.com/v2/venues/search";
            String version = "20130815";
            String urlString = String.format("%s?client_id=%s&client_secret=%s&v=%s&ll=%f,%f&intent=match",
            base,client,secret,version,latCoord,longCoord);

            //resp += "<br><br><br>"+urlString+"<br><br><br>";
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();

            String body = convertStreamToString(conn.getInputStream());

            Document doc  = Document.parse(body);
            Document response = doc.get("response", Document.class);
        //    resp+= "<br><br><br>"+response.toString()+"<br><br><br>";

            ArrayList<Document> venues = response.get("venues", ArrayList.class);
        //    resp+= "<br><br><br>"+venues.toString()+"<br><br><br>";

            Document info = venues.get(0);
            //resp+= "<br><br><br>"+info.toString()+"<br><br><br>";

            String name = info.get("name",String.class);
            Document location =  info.get("location",Document.class);
            String address = location.get("formattedAddress",String.class);

            retval = new String[2];
            retval[0] = name;
            retval[1] = address;
        }
        catch(Exception e)
        {
            System.out.println(e.getLocalizedMessage());
            //resp += "<br><br><br>"+e.getLocalizedMessage()+"<br><br><br>";
        }

        return retval;

    }


    public String convertStreamToString(InputStream is)
    {
            Scanner s = new Scanner(is).useDelimiter("\\A");
            String retval = s.hasNext() ? s.next() : null;

            s.close();
            return retval;
    }


    public void destroy()
    {
        if(user != null)
            user.closeConnection();
    }
}
