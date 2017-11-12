/*
 * Created by Guilherme Ziolle
 * Copyright (c) 2017. All rights reserved
 */

package com.example.gziolle.popmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents an item displayed in the movies list.
 */

class MovieItem implements Parcelable{

    private static final String AUTHORITY = "http://image.tmdb.org/t/p/w185";
    
    private long mId;
    private String mTitle;
    private String mPosterPath;
    private String mOverview;
    private double mAverage;
    private String mReleaseDate;


    MovieItem(long mId, String original_mTitle, String mPosterPath, String mOverview, double vote_mAverage, String mReleaseDate) {
        this.mId = mId;
        this.mTitle = original_mTitle;
        this.mPosterPath = AUTHORITY + mPosterPath;
        this.mOverview = mOverview;
        this.mAverage = vote_mAverage;
        this.mReleaseDate = mReleaseDate;
    }

    private MovieItem(Parcel in){
        this.mId = in.readLong();
        this.mTitle = in.readString();
        this.mPosterPath = in.readString();
        this.mOverview = in.readString();
        this.mAverage = in.readDouble();
        this.mReleaseDate = in.readString();
    }

    long getId() {
        return mId;
    }

    String getTitle() {
        return mTitle;
    }

    String getPosterPath() {
        return mPosterPath;
    }

    String getOverview() {
        return mOverview;
    }

    double getAverage() {
        return mAverage;
    }

    String getReleaseDate() {
        return mReleaseDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeString(mTitle);
        dest.writeString(mPosterPath);
        dest.writeString(mOverview);
        dest.writeDouble(mAverage);
        dest.writeString(mReleaseDate);
    }

    public static final Parcelable.Creator<MovieItem> CREATOR = new Parcelable.Creator<MovieItem>() {
        public MovieItem createFromParcel(Parcel in) {
            return new MovieItem(in);
        }

        public MovieItem[] newArray(int size) {
            return new MovieItem[size];
        }
    };
}
