package wangdaye.com.geometricweather.model.data;

import wangdaye.com.geometricweather.model.database.entity.LocationEntity;

/**
 * Location.
 * */

public class Location {
    // data
    public String name;
    public String realName;

    public Weather weather;
    public History history;

    /** <br> life cycle. */

    private Location() {
        this(null, null, null, null);
    }

    public Location(String name, String realName) {
        this(name, realName, null, null);
    }

    public Location(String name, String realName, Weather weather, History history) {
        this.name = name;
        this.realName = realName;
        this.weather = weather;
        this.history = history;
    }

    public static Location build(LocationEntity entity) {
        Location l = new Location();
        l.name = entity.location;
        l.realName = entity.realLocation;
        return l;
    }

    /** <br> utils. */

    public static boolean isEngLocation(String locationName) {
        locationName = locationName.replaceAll("，", ",");
        return locationName.getBytes().length == locationName.length();
    }
}
