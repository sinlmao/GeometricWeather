package wangdaye.com.geometricweather.model.database.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.greenrobot.greendao.query.QueryBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.model.data.History;
import wangdaye.com.geometricweather.model.data.Location;
import wangdaye.com.geometricweather.model.data.Weather;
import wangdaye.com.geometricweather.model.database.entity.DaoMaster;
import wangdaye.com.geometricweather.model.database.entity.HistoryEntity;
import wangdaye.com.geometricweather.model.database.entity.HistoryEntityDao;
import wangdaye.com.geometricweather.model.database.entity.LocationEntity;
import wangdaye.com.geometricweather.model.database.entity.LocationEntityDao;
import wangdaye.com.geometricweather.model.database.entity.WeatherEntity;
import wangdaye.com.geometricweather.model.database.entity.WeatherEntityDao;

/**
 * Database helper
 * */

public class DatabaseHelper {
    // data
    private DaoMaster.DevOpenHelper helper;
    private final static String DATABASE_NAME = "Geometric_Weather_db";
    private final String LOCAL;

    /** <br> life cycle. */

    private DatabaseHelper(Context c) {
        helper = new DaoMaster.DevOpenHelper(c, DATABASE_NAME);
        LOCAL = c.getString(R.string.local);
    }

    /** <br> database. */

    private SQLiteDatabase getDatabase() {
        return helper.getWritableDatabase();
    }

    /** <br> location. */

    // insert.

    public void insertLocation(Location l) {
        LocationEntity entity = searchLocationEntity(l);
        if (entity == null) {
            new DaoMaster(getDatabase())
                    .newSession()
                    .getLocationEntityDao()
                    .insert(LocationEntity.build(l));
        } else {
            entity.location = l.location;
            entity.realLocation = l.realLocation;
            updateLocation(entity);
        }
    }

    public void writeLocation(List<Location> l) {
        clearLocation();
        List<LocationEntity> entityList = new ArrayList<>();
        for (int i = 0; i < l.size(); i ++) {
            entityList.add(LocationEntity.build(l.get(i)));
        }
        new DaoMaster(getDatabase())
                .newSession()
                .getLocationEntityDao()
                .insertInTx(entityList);
    }

    // delete.

    public void deleteLocation(Location l) {
        LocationEntity entity = searchLocationEntity(l);
        if (entity != null) {
            new DaoMaster(getDatabase())
                    .newSession()
                    .getLocationEntityDao()
                    .delete(entity);
        }
    }

    public void clearLocation() {
        new DaoMaster(getDatabase())
                .newSession()
                .getLocationEntityDao()
                .deleteAll();
    }

    // update

    public void updateLocation(LocationEntity entity) {
        new DaoMaster(getDatabase())
                .newSession()
                .getLocationEntityDao()
                .update(entity);
    }

    // search.

    public Location searchLocation(Location l) {
        LocationEntity entity = searchLocationEntity(l);
        if (entity == null) {
            return null;
        } else {
            return Location.build(entity);
        }
    }

    public LocationEntity searchLocationEntity(Location l) {
        LocationEntityDao dao = new DaoMaster(getDatabase())
                .newSession()
                .getLocationEntityDao();

        QueryBuilder<LocationEntity> builder = dao.queryBuilder();
        builder.where(LocationEntityDao.Properties.Location.eq(l.location));

        List<LocationEntity> entityList = builder.list();
        if (entityList == null || entityList.size() <= 0) {
            return null;
        } else {
            return entityList.get(0);
        }
    }

    public List<Location> readLocation() {
        List<LocationEntity> entityList = new DaoMaster(getDatabase())
                .newSession()
                .getLocationEntityDao()
                .queryBuilder()
                .list();
        List<Location> locationList = new ArrayList<>();
        for (int i = 0; i < entityList.size(); i ++) {
            locationList.add(
                    new Location(
                            entityList.get(i).location,
                            entityList.get(i).realLocation));
        }
        if (locationList.size() <= 0) {
            locationList.add(new Location(LOCAL, null));
        }
        return locationList;
    }

    /** <br> weather. */

    // insert.

    public void insertWeather(Location l) {
        WeatherEntity entity = searchWeatherEntity(l);
        WeatherEntity newEntity = WeatherEntity.build(l.weather);
        if (entity != null) {
            deleteWeather(entity);
            newEntity.id = entity.id;
        }
        new DaoMaster(getDatabase())
                .newSession()
                .getWeatherEntityDao()
                .insert(newEntity);
    }

    public void deleteWeather(WeatherEntity entity) {
        new DaoMaster(getDatabase())
                .newSession()
                .getWeatherEntityDao()
                .delete(entity);
    }

    public Weather searchWeather(Location l) {
        WeatherEntity entity = searchWeatherEntity(l);
        if (entity == null) {
            return null;
        } else {
            return Weather.build(entity);
        }
    }

    public WeatherEntity searchWeatherEntity(Location l) {
        WeatherEntityDao dao = new DaoMaster(getDatabase())
                .newSession()
                .getWeatherEntityDao();

        QueryBuilder<WeatherEntity> builder = dao.queryBuilder();
        builder.where(
                WeatherEntityDao.Properties.Location.eq(
                        l.location.equals(LOCAL) ? l.realLocation : l.location));

        List<WeatherEntity> entityList = builder.list();
        if (entityList == null || entityList.size() <= 0) {
            return null;
        } else {
            return entityList.get(0);
        }
    }

    /** <br> history. */

    // insert.

    public void insertHistory(Weather w) {
        History yesterday = searchYesterdayHistory(w);
        clearLocationHistory(w);

        HistoryEntityDao dao = new DaoMaster(getDatabase())
                .newSession()
                .getHistoryEntityDao();
        if (yesterday != null) {
            dao.insert(HistoryEntity.build(yesterday));
        }
        dao.insert(HistoryEntity.build(History.build(w)));
    }

    // delete.

    public void clearLocationHistory(Weather w) {
        List<HistoryEntity> entityList = searchHistoryEntity(w);
        HistoryEntityDao dao = new DaoMaster(getDatabase())
                .newSession()
                .getHistoryEntityDao();
        for (int i = 0; i < entityList.size(); i ++) {
            dao.delete(entityList.get(i));
        }
    }

    @SuppressLint("SimpleDateFormat")
    public History searchYesterdayHistory(Weather w) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date date = format.parse(w.base.date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DATE, -1);

            HistoryEntityDao dao = new DaoMaster(getDatabase())
                    .newSession()
                    .getHistoryEntityDao();

            QueryBuilder<HistoryEntity> builder = dao.queryBuilder();
            builder.where(
                    HistoryEntityDao.Properties.Location.eq(w.base.location),
                    HistoryEntityDao.Properties.Date.eq(format.format(calendar.getTime())));

            List<HistoryEntity> entityList = builder.list();
            if (entityList == null || entityList.size() <= 0) {
                return null;
            } else {
                return History.build(entityList.get(0));
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<HistoryEntity> searchHistoryEntity(Weather w) {
        return new DaoMaster(getDatabase())
                .newSession()
                .getHistoryEntityDao()
                .queryBuilder()
                .where(HistoryEntityDao.Properties.Location.eq(w.base.location))
                .list();
    }

    /** <br> singleton. */

    private static DatabaseHelper instance;

    public static DatabaseHelper getInstance(Context c) {
        if (instance == null) {
            synchronized (DatabaseHelper.class) {
                instance = new DatabaseHelper(c);
            }
        }
        return instance;
    }
}
