import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;


public class UpdateUser extends HttpServlet
{
    protected BeaconUser user;

    public void init() throws ServletException
    {
        user = new BeaconUser();
    }


}
