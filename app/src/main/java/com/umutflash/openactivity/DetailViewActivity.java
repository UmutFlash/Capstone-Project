package com.umutflash.openactivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.umutflash.openactivity.adapter.FavorietsAdapter;
import com.umutflash.openactivity.data.AppDatabase;
import com.umutflash.openactivity.data.model.Spot;
import com.umutflash.openactivity.data.model.SpotEntry;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailViewActivity extends AppCompatActivity {

    @BindView(R.id.title)
    TextView titleTextView;
    @BindView(R.id.category)
    TextView categoryTextView;
    @BindView(R.id.discription)
    TextView descriptionTextView;
    @BindView(R.id.photo)
    ImageView imageView;
    @BindView(R.id.collapsing)
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.fab)
    FloatingActionButton mFab;

    private String mId;
    private String mSpotId;
    private String mUserId;
    private String mTitle;
    private String mCategory;
    private String mDescription;
    private String mImageUrl;
    private double mLatitude;
    private double mLongitude;
    private boolean isFavorite = false;

    private AppDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        Spot spot = getIntent().getParcelableExtra(FavorietsAdapter.ARG_Spot);
        if (spot != null) {
            mLatitude = spot.getLatitude();
            mLongitude = spot.getLongitude();
            mSpotId = spot.getSpotId();
            mTitle = spot.getTitle();
            mCategory = spot.getCategory();
            mDescription = spot.getDescription();
            mImageUrl = spot.getImageUrl();
            titleTextView.setText(spot.getTitle());

            categoryTextView.setText(mCategory);
            descriptionTextView.setText(mDescription);
            mCollapsingToolbarLayout.setTitle(mTitle);
            Glide.with(this)
                    .load(mImageUrl)
                    .centerCrop()
                    .into(imageView);
        }

        mDb = AppDatabase.getAppDatabase(getApplicationContext());

        getFavorites();

        mFab.setOnClickListener(view -> {
            if (isFavorite) {
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        mDb.favoritesDao().deleteFavoritesEntry(mSpotId);
                    }
                });
                isFavorite = false;
                mFab.setImageResource(R.drawable.ic_bookmark_o);
                Snackbar.make(view, "Removed from Favorites", Snackbar.LENGTH_LONG)
                        .show();
            } else {

                final SpotEntry favoritesEntry = new SpotEntry(mSpotId, mUserId, mTitle, mCategory, mDescription, mImageUrl, mLatitude, mLongitude, true);
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        mDb.favoritesDao().insert(favoritesEntry);
                    }
                });
                isFavorite = true;
                mFab.setImageResource(R.drawable.ic_bookmark);
                Snackbar.make(view, "Add to Favorites", Snackbar.LENGTH_LONG)
                        .show();
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void handleFavoriteBtn() {
        if (isFavorite) {
            mFab.setImageResource(R.drawable.ic_bookmark);
        } else {
            mFab.setImageResource(R.drawable.ic_bookmark_o);
        }
    }

    private void getFavorites() {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final List<SpotEntry> favoritesMovies = mDb.favoritesDao().loadAllFavorites();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!favoritesMovies.isEmpty()) {
                            isFavorite = false;
                            for (SpotEntry favorites : favoritesMovies) {
                                if (favorites.getSpotId().equals(mSpotId)) {
                                    isFavorite = true;
                                }
                            }
                            handleFavoriteBtn();
                        }
                    }
                });
            }
        });
    }

}
