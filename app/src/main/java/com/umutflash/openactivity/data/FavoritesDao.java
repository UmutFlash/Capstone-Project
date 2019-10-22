package com.umutflash.openactivity.data;


import com.umutflash.openactivity.data.model.SpotEntry;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;


@Dao
public abstract class FavoritesDao{

    @Query("SELECT * FROM favorites")
    public abstract LiveData<List<SpotEntry>> loadAllFavoritesSpots();

    @Query("SELECT * FROM favorites")
    public abstract List<SpotEntry> loadAllFavorites();


    @Query("DELETE FROM favorites WHERE spotId = :id")
    public abstract void deleteFavoritesEntry(String id);

    @Query("SELECT * FROM favorites WHERE spotId = :id")
    public abstract SpotEntry getFavorite(int id);

    @Query("SELECT isFavorite FROM favorites WHERE spotId = :spotId")
    public abstract boolean isFavorite(int spotId);

    @Query("SELECT COUNT(isFavorite) FROM favorites WHERE spotId = :spotId")
    public abstract int getCount(int spotId);

    @Insert
    public abstract void insert(SpotEntry favorites);

    @Delete
    public abstract void delete(SpotEntry favorites);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    public abstract void update(SpotEntry favorites);

}
