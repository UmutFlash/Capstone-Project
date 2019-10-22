package com.umutflash.openactivity;



import android.app.Application;

import com.umutflash.openactivity.data.AppDatabase;
import com.umutflash.openactivity.data.model.SpotEntry;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class FavoritesViewModel extends AndroidViewModel {

    private static final String TAG = FavoritesViewModel.class.getSimpleName();
    private LiveData<List<SpotEntry>> favoritesMovies;

    public FavoritesViewModel(@NonNull Application application) {
        super(application);

        AppDatabase appDatabase = AppDatabase.getAppDatabase(this.getApplication());
        favoritesMovies = appDatabase.favoritesDao().loadAllFavoritesSpots();

    }
    public LiveData<List<SpotEntry>> getFavorites() {
        return favoritesMovies;
    }
}
