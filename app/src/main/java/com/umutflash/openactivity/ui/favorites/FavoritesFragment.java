package com.umutflash.openactivity.ui.favorites;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

import com.google.firebase.database.DatabaseReference;
import com.umutflash.openactivity.AppWidget;
import com.umutflash.openactivity.R;
import com.umutflash.openactivity.adapter.FavorietsAdapter;
import com.umutflash.openactivity.data.model.Spot;
import com.umutflash.openactivity.data.model.SpotEntry;

import java.util.List;

public class FavoritesFragment extends Fragment {

    private FavoritesViewModel favorietsViewModel;

    @BindView(R.id.favorietsList)
    RecyclerView favorietRecyclerView;

    @BindView(R.id.no_favorites_layout)
    LinearLayout mNoFavoritesLayout;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_favorites, container, false);
        ButterKnife.bind(this, root);

        favorietRecyclerView.setHasFixedSize(true);
        favorietRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        getFavorites();

        if (savedInstanceState == null) {

        } else {
            Parcelable[] parcelableSpots = savedInstanceState.getParcelableArray(FavorietsAdapter.ARG_Spot);
            if (parcelableSpots != null) {
                Spot[] spotCollection = new Spot[parcelableSpots.length];
                for (int i = 0; i < parcelableSpots.length; i++) {
                    spotCollection[i] = (Spot) parcelableSpots[i];
                }
                favorietRecyclerView.setAdapter(new FavorietsAdapter(getContext(), spotCollection));
            }
        }
        return root;
    }


    private void getFavorites() {
        com.umutflash.openactivity.FavoritesViewModel mainViewModel = ViewModelProviders.of(this).get(com.umutflash.openactivity.FavoritesViewModel.class);
        mainViewModel.getFavorites().observe(this, new Observer<List<SpotEntry>>() {
            @Override
            public void onChanged(@Nullable List<SpotEntry> favoritesEntries) {
                Spot[] favorites = parseMovieArray(favoritesEntries);
                if (favoritesEntries != null && !favoritesEntries.isEmpty()) {
                    favorietRecyclerView.setAdapter(new FavorietsAdapter(getContext(), favorites));
                    mNoFavoritesLayout.setVisibility(View.GONE);

                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getContext());
                    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(getContext(), AppWidget.class));
                    AppWidget.onUpdateNewAppWidget(getContext(), appWidgetManager, favorites, appWidgetIds);
                } else {
                    favorietRecyclerView.setAdapter(null);
                    mNoFavoritesLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private Spot[] parseMovieArray(List<SpotEntry> favoritesMovies) {
        int resultsLength = favoritesMovies.size();
        Spot[] spots = new Spot[resultsLength];
        for (int i = 0; i < resultsLength; i++) {
            spots[i] = new Spot();
            String title = favoritesMovies.get(i).getTitle();
            spots[i].setTitle(title);
            String id = favoritesMovies.get(i).getSpotId();
            spots[i].setSpotId(id);
            spots[i].setCategory(favoritesMovies.get(i).getCategory());
            spots[i].setDescription(favoritesMovies.get(i).getDescription());
            spots[i].setImageUrl(favoritesMovies.get(i).getImageUrl());
            spots[i].setLatitude(favoritesMovies.get(i).getLatitude());
            spots[i].setLongitude(favoritesMovies.get(i).getLongitude());
        }
        return spots;
    }
}