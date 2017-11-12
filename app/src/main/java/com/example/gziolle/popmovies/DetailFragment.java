/*
 * Created by Guilherme Ziolle
 * Copyright (c) 2017. All rights reserved
 */

package com.example.gziolle.popmovies;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Hosts the "Details" layout.
 * It is inflated by DetailActivity.
 */

public class DetailFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            bindView(bundle);
        }
    }


    /*Binds the data from the a bundle to the elements in the layout*/
    public void bindView(Bundle bundle) {

        TextView title = (TextView) getActivity().findViewById(R.id.title);
        title.setText(bundle.getString(MovieListFragment.TMDB_TITLE));

        ImageView moviePoster = (ImageView) getActivity().findViewById(R.id.movie_image);
        String posterUrl = bundle.getString(MovieListFragment.TMDB_POSTER_PATH);
        Picasso.with(getActivity()).load(posterUrl)
                .error(R.mipmap.ic_launcher).fit().into(moviePoster);

        TextView releaseDate = (TextView) getActivity().findViewById(R.id.release_date);
        releaseDate.setText(bundle.getString(MovieListFragment.TMDB_RELEASE_DATE));

        TextView voteAverage = (TextView) getActivity().findViewById(R.id.average);
        double averageScore = bundle.getDouble(MovieListFragment.TMDB_VOTE_AVERAGE);
        voteAverage.setText(String.format(getActivity().getString(R.string.average_score), averageScore));

        TextView overview = (TextView) getActivity().findViewById(R.id.overview);
        overview.setText(bundle.getString(MovieListFragment.TMDB_OVERVIEW));

    }
}
