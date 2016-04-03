package beacon.api;

import beacon.*;
import beacon.util.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import java.util.ArrayList;

public class UpdateUser extends HttpServlet
{
    protected BeaconUser user;
    protected static String MONGO_HOST;
    protected static String DB_NAME;

    public void init() throws ServletException
    {
        user = new BeaconUser(MONGO_HOST,DB_NAME);
        Paths.setUp();

        MONGO_HOST = Paths.getHostName();
        DB_NAME = Paths.getDBName();
    }

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
              throws ServletException, IOException
    {
        response.setContentType("text/html");

        boolean success = false;
        String uid = request.getParameter("uid");
        if(uid != null)
        {
            double latCoord = Double.parseDouble(request.getParameter("lat"));
            double longCoord = Double.parseDouble(request.getParameter("long"));

            success = updateUser(uid, latCoord, longCoord);
        }

        PrintWriter out = response.getWriter();

        out.println("<p>"+((success) ? "Success" : "Failure")+"</p>");
    }

    /**
     * Updates the user's location with the given coordinates
     * @param   String  The Username or ID of the User to update
     * @param   double  The current latitude coordinate of the user
     * @param   double  The current longitude coordinate of the user
     * @return  True if user was successfully updated
     */
    public boolean updateUser(String uid, double latCoord, double longCoord)
    {
        String response = user.getUserByName(uid);

        boolean retval = false;

        if(response != null)
            retval = user.updateLastLocation(latCoord,longCoord);
        else
        {
            user = new BeaconUser(MONGO_HOST, DB_NAME,uid,"",
                    new ArrayList<String>(),latCoord,longCoord);
            retval = user.insert();
        }//else

        return retval;
    }//updateUser(String,double,double)

    public void destroy()
    {
        if(user != null)
            user.closeConnection();
    }
}//UpdateUser class