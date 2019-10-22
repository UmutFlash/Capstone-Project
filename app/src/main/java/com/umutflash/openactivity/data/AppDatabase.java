package com.umutflash.openactivity.data;


import android.content.Context;
import android.util.Log;

import com.umutflash.openactivity.data.model.SpotEntry;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;


@Database(entities = {SpotEntry.class}, version = 1, exportSchema = true)
public abstract class AppDatabase extends RoomDatabase {


    private static final String LOG_TAG = AppDatabase.class.getSimpleName();
    private static final String DATABASE_NAME ="favorites";
    private static final Object LOG = new Object();

    private static AppDatabase INSTANCE;



    public static AppDatabase getAppDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, AppDatabase.DATABASE_NAME)
                            .build();
        }
        Log.d(LOG_TAG,"Getting the database instance");
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
    public abstract FavoritesDao favoritesDao();

}
