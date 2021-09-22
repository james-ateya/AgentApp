package com.paltouch.agentapp;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class parent_tab_adapter extends FragmentPagerAdapter {
    private Context myContext;
    int totalTabs;

    public parent_tab_adapter(Context context, FragmentManager fm, int totalTabs) {
        super(fm);
        myContext = context;
        this.totalTabs = totalTabs;
    }

    // this is for fragment tabs
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                //HomeFragment homeFragment = new HomeFragment();
                //return homeFragment;
            case 1:
                //SportFragment sportFragment = new SportFragment();
                //return sportFragment;
            default:
                return null;
        }
    }
    // this counts total number of tabs
    @Override
    public int getCount() {
        return totalTabs;
    }
}
