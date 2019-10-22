package com.umutflash.openactivity;

import android.os.Bundle;
import android.os.Parcelable;

import com.google.firebase.database.DatabaseReference;
import com.umutflash.openactivity.adapter.FavorietsAdapter;
import com.umutflash.openactivity.data.model.Spot;
import com.umutflash.openactivity.data.model.SpotEntry;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class FavorietsActivity extends AppCompatActivity {

    @BindView(R.id.favorietsList)
    RecyclerView favorietRecyclerView;
    private FavorietsAdapter mAdapter;
    private DatabaseReference mDataRef;
    private List<SpotEntry> favoriets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        ButterKnife.bind(this);


        favorietRecyclerView.setHasFixedSize(true);
        favorietRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        getFavorites();


        if (savedInstanceState == null) {
            //show snachbar
        } else {
            Parcelable[] parcelableSpots = savedInstanceState.getParcelableArray(FavorietsAdapter.ARG_Spot);
            if (parcelableSpots != null) {
                Spot[] spotCollection = new Spot[parcelableSpots.length];
                for (int i = 0; i < parcelableSpots.length; i++) {
                    spotCollection[i] = (Spot) parcelableSpots[i];
                }
                favorietRecyclerView.setAdapter(new FavorietsAdapter(getApplicationContext(), spotCollection));
            }
        }


        /*

        favoriets = new ArrayList<>();
        mDataRef = FirebaseDatabase.getInstance().getReference("spots");

        mDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    SpotEntry spot = postSnapshot.getValue(SpotEntry.class);
                    favoriets.add(spot);

                }
                mAdapter = new FavorietsAdapter(FavorietsActivity.this, favoriets);

                favorietRecyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

         */

    }

    private void getFavorites() {
        FavoritesViewModel mainViewModel = ViewModelProviders.of(this).get(FavoritesViewModel.class);
        mainViewModel.getFavorites().observe(this, new Observer<List<SpotEntry>>() {
            @Override
            public void onChanged(@Nullable List<SpotEntry> favoritesEntries) {
                Spot[] favorites = parseMovieArray(favoritesEntries);
                if (favoritesEntries != null && !favoritesEntries.isEmpty()) {
                    favorietRecyclerView.setAdapter(new FavorietsAdapter(FavorietsActivity.this, favorites));
                } else {
                    favorietRecyclerView.setAdapter(null);
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
