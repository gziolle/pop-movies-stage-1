/*
 * Created by Guilherme Ziolle
 * Copyright (c) 2017. All rights reserved
 */

package com.example.gziolle.popmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Binds the movie list (RecyclerView) to actual data, retrieved through the TheMovieDB API.
 */

class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private static final String TAG = MovieAdapter.class.getSimpleName();

    private Context mContext;
    private ArrayList<MovieItem> mMovieList;

    private final ListItemClickListener mClickListener;

    interface ListItemClickListener{
        void onListItemClicked(int position);
    }


    MovieAdapter(Context context, ListItemClickListener listener,ArrayList<MovieItem> movieList) {
        this.mContext = context;
        this.mMovieList = movieList;
        this.mClickListener = listener;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        View itemView = layoutInflater.inflate(R.layout.grid_view_item, parent, false);

        return new MovieViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        MovieItem movieItem = mMovieList.get(position);

        //Download the image using Picasso and save it to the ImageView
        Picasso.with(mContext).load(movieItem.getPosterPath())
                .error(R.mipmap.ic_launcher).fit().into(holder.mPosterImageView);
    }

    @Override
    public int getItemCount() {
        return mMovieList.size();
    }

    MovieItem getItem(int position){
        return mMovieList.get(position);
    }


    /** Holds references to the components of an item in the movie list.*/
    class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ImageView mPosterImageView;

        MovieViewHolder(View itemView){
            super(itemView);
            mPosterImageView = (ImageView) itemView.findViewById(R.id.iv_poster);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mClickListener.onListItemClicked(getAdapterPosition());
        }
    }
}

