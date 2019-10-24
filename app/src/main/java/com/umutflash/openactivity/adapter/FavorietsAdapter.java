package com.umutflash.openactivity.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.umutflash.openactivity.DetailViewActivity;
import com.umutflash.openactivity.R;
import com.umutflash.openactivity.data.model.Spot;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FavorietsAdapter extends RecyclerView.Adapter<FavorietsAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private Spot[] mSpotsList;

    public static final String ARG_Spot = "spot";

    public FavorietsAdapter(Context context, Spot[] spots) {
        this.mInflater = LayoutInflater.from(context);
        mSpotsList = spots;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.from(parent.getContext())
                .inflate(R.layout.favoriets_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Spot spotCurrent = mSpotsList[position];
        holder.mTitle.setText(spotCurrent.getTitle());
        holder.mCategory.setText(spotCurrent.getCategory());
        Uri imageFilePath = Uri.parse(spotCurrent.getImageUrl());
        Glide.with(holder.mView)
                .load(imageFilePath)
                .centerCrop()
                .into(holder.mImageView);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                Intent intent = new Intent(context, DetailViewActivity.class);
                intent.putExtra(ARG_Spot, spotCurrent);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mSpotsList.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mTitle;
        final TextView mCategory;
        ImageView mImageView;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mTitle = view.findViewById(R.id.title);
            mCategory = view.findViewById(R.id.category);
            mImageView = view.findViewById(R.id.picView);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTitle.getText();
        }
    }
}
