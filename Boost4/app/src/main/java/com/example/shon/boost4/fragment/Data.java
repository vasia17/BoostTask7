package com.example.shon.boost4.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.shon.boost4.MainActivity;
import com.example.shon.boost4.R;
import com.example.shon.boost4.adapter.DataRecAdapter;
import com.example.shon.boost4.entity.Sample;
import com.example.shon.boost4.listener.MyChildEventListener;
import com.example.shon.boost4.view.MyPlotView;

import java.util.ArrayList;
import java.util.List;

public class Data extends Fragment {

    private List<Sample> mSamples = new ArrayList<>();

    private RecyclerView mRecView;
    private MyPlotView mPlotView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View data = inflater.inflate(R.layout.frag_data, container, false);

        mRecView = (RecyclerView) data.findViewById(R.id.rv_samples);
        mRecView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecView.setAdapter(new DataRecAdapter(mSamples));

        mPlotView = (MyPlotView) data.findViewById(R.id.my_plot_View);
        mPlotView.setSamples(mSamples);

        if(MainActivity.sSamplesRef != null){
            MainActivity.sChildEventListener = MainActivity.sSamplesRef
                    .addChildEventListener(new MyChildEventListener(mRecView, mPlotView));
        }
        return data;
    }
}
