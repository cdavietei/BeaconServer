package beacon.util;

public enum Enums
{
    MONGO_HOST("localhost"),

    DB_NAME("beaconDB");

    private String value;

    private Enums(String value)
    {
        this.value = value;
    }

    public String toString()
    {
        return value;
    }
}
