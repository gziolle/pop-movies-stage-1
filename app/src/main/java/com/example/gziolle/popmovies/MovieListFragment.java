/*
 * Created by Guilherme Ziolle
 * Copyright (c) 2017. All rights reserved
 */

package com.example.gziolle.popmovies;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Creates the list of movies displayed to the user.
 * Handles item selection as well.
 */

public class MovieListFragment extends Fragment implements MovieAdapter.ListItemClickListener{

    public static final String LOG_TAG = MovieListFragment.class.getSimpleName();

    public static String TMDB_RESULTS = "results";
    public static String TMDB_ID = "id";
    public static String TMDB_TITLE = "title";
    public static String TMDB_POSTER_PATH = "poster_path";
    public static String TMDB_OVERVIEW = "overview";
    public static String TMDB_VOTE_AVERAGE = "vote_average";
    public static String TMDB_RELEASE_DATE = "release_date";

    String MOVIE_LIST_EXTRA = "movielist";

    public RecyclerView mRecyclerView;
    public GridLayoutManager mGridLayoutManager;
    public MovieAdapter mMovieAdapter;
    public ArrayList<MovieItem> mMovieItems;

    private int mCurrentPage = 0;
    private String mLastQueryMode = "";
    private boolean mIsFetching = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        int spanCount = calculateSpanCount(getActivity());

        View rootView = inflater.inflate(R.layout.fragment_movie_list, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_movie_list);

        mGridLayoutManager = new GridLayoutManager(getActivity(), spanCount);

        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.setLayoutManager(mGridLayoutManager);

        if(savedInstanceState != null){
            mMovieItems = savedInstanceState.getParcelableArrayList(MOVIE_LIST_EXTRA);
        } else{
            mMovieItems = new ArrayList<>();
        }

        mMovieAdapter = new MovieAdapter(getActivity(),this, mMovieItems);

        mRecyclerView.setAdapter(mMovieAdapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(dy > 0){
                    int visibleItemCount = mGridLayoutManager.getChildCount();
                    int totalItemCount = mGridLayoutManager.getItemCount();
                    int pastVisibleItems = mGridLayoutManager.findFirstVisibleItemPosition();

                    if(((pastVisibleItems + visibleItemCount) == totalItemCount) && !mIsFetching){
                        updateMovieList();
                    }
                }
            }
        });
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovieList();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(MOVIE_LIST_EXTRA, mMovieItems);
        super.onSaveInstanceState(outState);
    }

    /** Updates the movies list based on its current page.
    * It also stores the user's preference for future usage.*/
    public void updateMovieList() {
        ConnectivityManager manager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        if (networkInfo != null && (networkInfo.isAvailable() && networkInfo.isConnected())) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String queryMode = prefs.getString(getString(R.string.query_mode_key), getString(R.string.query_mode_default));
            if (mLastQueryMode.equals("")) {
                mLastQueryMode = queryMode;
                mCurrentPage = 1;
            } else if (!mLastQueryMode.equals(queryMode)) {
                mMovieItems.clear();
                mCurrentPage = 1;
                mLastQueryMode = queryMode;
            } else {
                mCurrentPage++;
            }

            new FetchMoviesTask().execute(queryMode, String.valueOf(mCurrentPage));

        } else {
            Toast.makeText(getActivity(), getActivity().getString(R.string.offline_status), Toast.LENGTH_SHORT).show();
        }
    }

    /** Callback method to handle item selection */
    @Override
    public void onListItemClicked(int position) {
        MovieItem item = mMovieAdapter.getItem(position);

        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra(TMDB_TITLE, item.getTitle());
        intent.putExtra(TMDB_POSTER_PATH, item.getPosterPath());
        intent.putExtra(TMDB_RELEASE_DATE, item.getReleaseDate());
        intent.putExtra(TMDB_OVERVIEW, item.getOverview());
        intent.putExtra(TMDB_VOTE_AVERAGE, item.getAverage());

        startActivity(intent);
    }

    public static int calculateSpanCount(Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float dpWidth = metrics.widthPixels / metrics.density;
        return (int) (dpWidth / 180);
    }

    /**
     * Retrieves movie data from the TheMovieDB database through its API.
     */
    private class FetchMoviesTask extends AsyncTask<String, Void, ArrayList<MovieItem>> {

        private String TMDB_AUTHORITY = "api.themoviedb.org";
        private String TMDB_API_VERSION = "3";
        private String TMDB_MOVIE_DIR = "movie";
        private String TMDB_API_KEY = "api_key";
        private String TMDB_LANGUAGE = "language";
        private String TMDB_PAGE = "page";

        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage(getActivity().getString(R.string.progress_dialog_loading));
            mProgressDialog.setCancelable(true);
            mProgressDialog.show();
        }

        @Override
        protected ArrayList<MovieItem> doInBackground(String... params) {

            HttpURLConnection conn = null;
            InputStream is;
            BufferedReader reader = null;
            String moviesJSONString;
            ArrayList<MovieItem> movieItems = null;

            if (params[0] == null) {
                return null;
            }

            mIsFetching = true;

            String queryMode = params[0];
            String mCurrentPage = params[1];

            try {
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http");
                builder.authority(TMDB_AUTHORITY);
                builder.appendPath(TMDB_API_VERSION).appendPath(TMDB_MOVIE_DIR).appendPath(queryMode);
                builder.appendQueryParameter(TMDB_API_KEY, BuildConfig.THE_MOVIE_DB_KEY);
                builder.appendQueryParameter(TMDB_LANGUAGE, "en-us");
                builder.appendQueryParameter(TMDB_PAGE, mCurrentPage);

                URL queryUrl = new URL(builder.build().toString());

                conn = (HttpURLConnection) queryUrl.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                is = conn.getInputStream();

                /*Returns null if the connection could not get an InputStream*/
                if (is == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(is));

                String line;
                StringBuilder inputStreamBuilder = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    inputStreamBuilder.append(line);
                    inputStreamBuilder.append("\n");

                }

                /*Returns null if nothing comes from the server*/
                if (inputStreamBuilder.length() == 0) {
                    return null;
                }

                moviesJSONString = inputStreamBuilder.toString();

                movieItems = getDataFromJSON(moviesJSONString);

            } catch (IOException | JSONException ex) {
                Log.e(LOG_TAG, ex.getMessage());
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }

                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e) {
                        Log.e(LOG_TAG, e.getMessage());
                    }
                }
            }
            return movieItems;
        }

        @Override
        protected void onPostExecute(ArrayList<MovieItem> result) {
            mIsFetching = false;
            mProgressDialog.dismiss();
            if (result != null) {
                mMovieItems.addAll(result);
                mMovieAdapter.notifyDataSetChanged();
            }
            super.onPostExecute(result);
        }

        /** Converts the data from the JSON Object into a MovieItem list */
        ArrayList<MovieItem> getDataFromJSON(String JSONString) throws JSONException {
            ArrayList<MovieItem> movieItems = new ArrayList<>();

            JSONObject mainObject = new JSONObject(JSONString);

            JSONArray moviesArray = mainObject.getJSONArray(TMDB_RESULTS);

            for (int i = 0; i < moviesArray.length(); i++) {
                JSONObject movie = moviesArray.getJSONObject(i);
                MovieItem item = new MovieItem(movie.getLong(TMDB_ID), movie.getString(TMDB_TITLE),
                        movie.getString(TMDB_POSTER_PATH), movie.getString(TMDB_OVERVIEW),
                        movie.getDouble(TMDB_VOTE_AVERAGE), movie.getString(TMDB_RELEASE_DATE));
                movieItems.add(item);
            }
            return movieItems;
        }
    }
}
