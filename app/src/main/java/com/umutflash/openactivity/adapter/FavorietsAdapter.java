package com.umutflash.openactivity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;
import com.umutflash.openactivity.R;
import com.umutflash.openactivity.data.SpotInformation;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FavorietsAdapter extends RecyclerView.Adapter<FavorietsAdapter.ViewHolder>{

    private LayoutInflater mInflater;
    private Context mContext;
    private List<SpotInformation> mSpotInformationList;

    public FavorietsAdapter(Context context, List<SpotInformation> spots) {
        this.mInflater = LayoutInflater.from(context);
        mContext = context;
        mSpotInformationList = spots;
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
        SpotInformation spotCurrent = mSpotInformationList.get(position);
        holder.mTitle.setText(spotCurrent.getTitle());
        Picasso.get()
                .load(spotCurrent.getImageUrl())
                .into(holder.mImageView);
    }

    @Override
    public int getItemCount() {
        return mSpotInformationList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mTitle;
        ImageView mImageView;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mTitle = view.findViewById(R.id.title);
            mImageView = view.findViewById(R.id.picView);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTitle.getText();
        }
    }
}
