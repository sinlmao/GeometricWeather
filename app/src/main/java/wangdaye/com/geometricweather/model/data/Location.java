package wangdaye.com.geometricweather.model.data;

import wangdaye.com.geometricweather.model.database.entity.LocationEntity;

/**
 * Location.
 * */

public class Location {
    // data
    public String location;
    public String realLocation;

    public Weather weather;
    public History history;

    /** <br> life cycle. */

    private Location() {
        this(null, null, null, null);
    }

    public Location(String location, String realLocation) {
        this(location, realLocation, null, null);
    }

    public Location(String location, String realLocation, Weather weather, History history) {
        this.location = location;
        this.realLocation = realLocation;
        this.weather = weather;
        this.history = history;
    }

    public static Location build(LocationEntity entity) {
        Location l = new Location();
        l.location = entity.location;
        l.realLocation = entity.realLocation;
        return l;
    }

    /** <br> utils. */

    public static boolean isEngLocation(String locationName) {
        //return locationName.replaceAll(" ", "").matches("[a-zA-Z]+");
        locationName = locationName.replaceAll("，", ",");
        return locationName.getBytes().length == locationName.length();
    }
}
