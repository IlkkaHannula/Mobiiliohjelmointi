package com.example.android.sunshine;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Ile on 28.2.2018.
 */

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder> {

    private String[] mWeatherData;

    public class ForecastAdapterViewHolder extends RecyclerView.ViewHolder {
        public final TextView mWeatherTextView;

        public ForecastAdapterViewHolder(View view) {
            super(view);
            mWeatherTextView = (TextView) view.findViewById(R.id.tv_weather_data);
        }
// TODO (29) Override getItemCount
// TODO (30) Return 0 if mWeatherData is null, or the size of mWeatherData if it is not null

// TODO (31) Create a setWeatherData method that saves the weatherData to mWeatherData
// TODO (32) After you save mWeatherData, call notifyDataSetChanged
    }

    public void setWeatherData(String[] weatherData){
        mWeatherData = weatherData;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mWeatherData == null){
            return 0;
        }
        return mWeatherData.length;
    }

    @Override
    public void onBindViewHolder(ForecastAdapterViewHolder holder, int position) {
        String mWeatherDatum = mWeatherData[position];
        holder.mWeatherTextView.setText(mWeatherDatum);
    }

    @Override
    public ForecastAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.forecast_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new ForecastAdapterViewHolder(view);
    }
}


// TODO (47) Create the default constructor (we will pass in parameters in a later lesson)

