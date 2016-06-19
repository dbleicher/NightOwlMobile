package com.geofinity.wgu.nightowl.ui.adapters;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.geofinity.wgu.nightowl.ui.FragCommunityMessages;
import com.geofinity.wgu.nightowl.ui.FragCourses;
import com.geofinity.wgu.nightowl.ui.FragHome;

/**
 * Created by davidbleicher on 8/31/14.
 */
public class MainPagerAdapter extends FragmentStatePagerAdapter {

    private FragmentManager fm;

    public MainPagerAdapter (FragmentManager fm) {
        super(fm);
        this.fm = fm;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new FragHome();
            case 1:
                return new FragCourses();
            case 2:
                return new FragCommunityMessages();
            default:
                return new FragHome();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

}
