package com.javalive09.apsharer.applist;

import android.view.ViewGroup;

import com.javalive09.apsharer.R;

public class AppListAdapter extends AppAdapter {

    public AppListAdapter(MainActivity act) {
        super(act);
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(factory.inflate(R.layout.listviewitem, parent, false));
    }

}