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
    protected static BeaconUser user;

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
        response.setContentType("text/html");

        boolean success = false;

        String uid = request.getParameter("uid");

        if(uid != null && user.getUserByName(uid) != null )
        {
            String title = request.getParameter("title");

            double latCoord = Double.parseDouble(request.getParameter("lat"));
            double longCoord = Double.parseDouble(request.getParameter("long"));                double range = Double.parseDouble(request.getParameter("range"));

            Date  start = new Date(Long.parseLong(request.getParameter("start")));
            Date  end = new Date(Long.parseLong(request.getParameter("end")));

            String[] tags = request.getParameterValues("tags");
            String[] address = getAddress(latCoord,longCoord);

            success = user.placeBeacon(title, latCoord, longCoord,start, end,
                        range, address[0], address[1],
                        new ArrayList<String>(Arrays.asList(tags)));
        }

        PrintWriter out = response.getWriter();
        out.println("<p>"+((success) ? "Success" : "Failure")+"</p>");

        out.close();

    }

    public String[] getAddress(double latCoord, double longCoord)
    {
        String[] retval = {"",""};
        try
        {
            String client = "X4Z021O0Q1QNG3BRTNAZBG5WWZPHQEXYA3NZW40AHJ1JSQJM";
            String secret = "OP344JWOZIAQU5YYEDZOEBYDBEBKRJMFY1BQWUO3KC4ZEDHM";
            String base = "https://api.foursquare.com/v2/venues/search";
            String version = "20130815";
            String urlString = String.format("%s?client_id=%s&client_secret=%s&v=%s&ll=%d,%d",
            base,client,secret,version,latCoord,longCoord);

            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();

            String body = convertStreamToString(conn.getInputStream());

            Document doc  = Document.parse(body);
            Document response = doc.get("response", Document.class);
            ArrayList<Document> venues = response.get("venues", ArrayList.class);
            Document info = venues.get(0);
            String name = info.get("name",String.class);
            Document location =  info.get("location",Document.class);
            String address = location.get("address",String.class);

            retval = new String[2];
            retval[0] = name;
            retval[1] = address;
        }
        catch(Exception e)
        {
            System.out.println(e.getLocalizedMessage());
        }

        return retval;

    }


    public String convertStreamToString(InputStream is)
    {
            Scanner s = new Scanner(is).useDelimiter("\\A");
            String retval = s.hasNext() ? s.next() : "";

            s.close();
            return retval;
    }


    public void destroy()
    {
        if(user != null)
            user.closeConnection();
    }
}
