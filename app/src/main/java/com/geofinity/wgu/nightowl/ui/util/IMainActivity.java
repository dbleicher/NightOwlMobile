package com.geofinity.wgu.nightowl.ui.util;


import android.support.v7.app.ActionBar;

/**
 * Created by davidbleicher on 8/31/14.
 */
public interface IMainActivity {

    public void navigateToPage(EMainFragments frag);
    public ActionBar getMyActionBar();
    public void authenticateIfNecessary();
    public String displayCommFilter();

    public void showMentorInfo();
    public void emailMyMentor();
}
