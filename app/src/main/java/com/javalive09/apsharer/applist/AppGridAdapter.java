package com.javalive09.apsharer.applist;

import android.view.ViewGroup;

import com.javalive09.apsharer.R;

public class AppGridAdapter extends AppAdapter {

    public AppGridAdapter(MainActivity act) {
        super(act);
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(factory.inflate(R.layout.gridviewitem, parent, false));
    }

}