package beacon.api;

import beacon.*;
import beacon.util.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;

public class FindNearBeacons extends HttpServlet
{
    protected static String MONGO_HOST;
    protected static String DB_NAME;
    protected BeaconUser user;

    public void init() throws ServletException
    {
        MONGO_HOST = Enums.MONGO_HOST.toString();
        DB_NAME = Enums.DB_NAME.toString();

        user = new BeaconUser(MONGO_HOST,DB_NAME);
    }

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
              throws ServletException, IOException
    {
        response.setContentType("application/json");

        if(request.getParameter("lat") != null
            && request.getParameter("long") != null)
        {
            double latCoord = Double.parseDouble(request.getParameter("lat"));
            double longCoord = Double.parseDouble(request.getParameter("long"));
            double distance = Double.parseDouble(request.getParameter("dist"));

            int max = Integer.parseInt(request.getParameter("max"));

            String json = null;

            if(request.getParameter("tags") == null)
                json = user.findNearbyBeacons(max, latCoord,
                            longCoord, distance);
            else
            {
                String[] tags = request.getParameterValues("tags");
                ArrayList<String> list = new ArrayList<String>(Arrays.asList(tags));

                json = user.findNearbyBeaconsByTags(max, latCoord,
                            longCoord, distance, list);
            }

            PrintWriter out = response.getWriter();

            out.println(json);
            out.close();
        }
    }

    public void destroy()
    {
        if(user != null)
            user.closeConnection();

    }
}
