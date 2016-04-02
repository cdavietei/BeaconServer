import beacon.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;


public class UpdateUser extends HttpServlet
{
    protected BeaconUser user;
    protected static final String MONGO_HOST = "localhost";
    protected static final String DB_NAME = "beaconDB";

    public void init() throws ServletException
    {
        user = new BeaconUser(MONGO_HOST,DB_NAME);
    }


    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
              throws ServletException, IOException
    {


    }


}
