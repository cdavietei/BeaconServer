package beacon.util;

import java.io.*;

public class Paths
{
    protected static String[] paths;
    protected static final int HOST_NAME = 0;
    protected static final int DB_NAME = 1;

    public static boolean setUp()
    {
        BufferedReader reader;

        boolean retval = false;

        try
        {
            File test = new File("test.txt");
            reader = new BufferedReader(new FileReader(new File("paths.txt")));

            int count = Integer.parseInt(reader.readLine());

            paths = new String[count];

            for(int i =0; i< count; i++)
                paths[i] = reader.readLine();
            reader.close();
            retval = true;
        }
        catch(IOException e)
        {
            System.out.println(e.getLocalizedMessage());
        }

        return retval;
    }

    public static String getHostName()
    {
        if(paths != null)
            return paths[HOST_NAME];
        else
            return null;
    }

    public static String getDBName()
    {
        if(paths != null)
            return paths[DB_NAME];
        else
            return null;
    }
}
