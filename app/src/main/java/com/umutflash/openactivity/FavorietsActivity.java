package com.umutflash.openactivity;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.umutflash.openactivity.adapter.FavorietsAdapter;
import com.umutflash.openactivity.data.SpotInformation;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class FavorietsActivity extends AppCompatActivity {

    @BindView(R.id.favorietsList)
    RecyclerView favorietRecyclerView;
    private FavorietsAdapter mAdapter;
    private DatabaseReference mDataRef;
    private List<SpotInformation> favoriets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favoriets);
        ButterKnife.bind(this);


        favorietRecyclerView.setHasFixedSize(true);
        favorietRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        favoriets = new ArrayList<>();
        mDataRef = FirebaseDatabase.getInstance().getReference("spots");

        mDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    SpotInformation spot = postSnapshot.getValue(SpotInformation.class);
                    favoriets.add(spot);

                }
                mAdapter = new FavorietsAdapter(FavorietsActivity.this, favoriets);

                favorietRecyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
