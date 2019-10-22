package com.umutflash.openactivity.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Spot implements Parcelable {

    private String spotId;
    private String userId;
    private String title;
    private String category;
    private String description;
    private String imageUrl;
    private double latitude;
    private double longitude;





    public Spot() {

    }

    public Spot(String spotId, String userId, String title, String category, String description, String imageUrl, double latitude, double longitude) {
        this.spotId = spotId;
        this.userId = userId;
        this.title = title;
        this.category = category;
        this.description = description;
        this.imageUrl = imageUrl;
        this.latitude = latitude;
        this.longitude = longitude;

    }

    protected Spot(Parcel in) {
        spotId = in.readString();
        userId = in.readString();
        title = in.readString();
        category = in.readString();
        description = in.readString();
        imageUrl = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public static final Creator<Spot> CREATOR = new Creator<Spot>() {
        @Override
        public Spot createFromParcel(Parcel in) {
            return new Spot(in);
        }

        @Override
        public Spot[] newArray(int size) {
            return new Spot[size];
        }
    };

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getSpotId() {
        return spotId;
    }

    public void setSpotId(String spotId) {
        this.spotId = spotId;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(spotId);
        parcel.writeString(userId);
        parcel.writeString(title);
        parcel.writeString(category);
        parcel.writeString(description);
        parcel.writeString(imageUrl);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
    }
}


