package com.geofinity.wgu.nightowl.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.geofinity.pwnet.events.ESocialFilter;
import com.geofinity.wgu.nightowl.NOMApp;
import com.geofinity.wgu.nightowl.R;
import com.geofinity.wgu.nightowl.model.PocketPreferences;
import com.geofinity.wgu.nightowl.model.SocialItem;
import com.geofinity.wgu.nightowl.ui.adapters.SocialAdapter;
import com.geofinity.wgu.nightowl.ui.dialogs.DiagSocialFilter;
import com.geofinity.wgu.nightowl.ui.util.QRBarDecoration;
import com.joanzapata.iconify.widget.IconButton;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by davidbleicher on 12/16/14.
 */
public class ActSocial extends ActionBarActivity {
    private RecyclerView sgv;
    private StaggeredGridLayoutManager sglm;
    private EventBus eBus;

    private LinearLayout socialToolBar;
    private TextView tvSocialDisplay;
    private IconButton ibFilter;

    private PocketPreferences prefs;
    private int columnCount;
    private int qrBarHeight;

    private Animation inAnim;
    private Animation outAnim;

    private SocialAdapter socialAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.act_social_listing);

        prefs = NOMApp.prefs;
        eBus = EventBus.getDefault();

        //////////////////////////
        //  Setup The Toolbar   //
        //////////////////////////
        inAnim = AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_top);
        outAnim = AnimationUtils.loadAnimation(this, R.anim.abc_slide_out_top);
        inAnim.setDuration(600);
        outAnim.setDuration(600);

        socialToolBar = (LinearLayout) findViewById(R.id.socialToolBar);
        tvSocialDisplay = (TextView) findViewById(R.id.tvSocialDisplay);
        ibFilter = (IconButton) findViewById(R.id.ibFilter);

        ibFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DiagSocialFilter cfl = new DiagSocialFilter();
                cfl.show(getSupportFragmentManager(), "socialfilterdialog");
            }
        });
        setFilterTitle();

        //////////////////////////////////////////////
        // Get number of columns and toolBar height //
        //////////////////////////////////////////////
        columnCount = getResources().getInteger(R.integer.sgv_column_count);
        columnCount++;
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
            qrBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }

        //////////////////////////////////////////////
        //  Grab the StaggeredGrid & LayoutManager  //
        //////////////////////////////////////////////
        sgv = (RecyclerView) findViewById(R.id.rvSocialList);
        sgv.addItemDecoration(new QRBarDecoration(columnCount, qrBarHeight));
        sgv.setHasFixedSize(true);

        sglm = new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        sgv.setLayoutManager(sglm);


        //////////////////////////////
        //  Setup Scroll & Adapter  //
        //////////////////////////////
        sgv.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                if (dy > 3) {
                    if (socialToolBar.getVisibility() == View.VISIBLE)
                        hideSocialToolBar();

                } else if (dy < -3) {

                    if (socialToolBar.getVisibility() == View.GONE)
                        showSocialToolBar();
                }
            }
        });

        socialAdapter = new SocialAdapter();
        sgv.setAdapter(socialAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        eBus.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        eBus.unregister(this);
    }

    private void showSocialToolBar() {

        socialToolBar.startAnimation(inAnim);
        socialToolBar.setVisibility(View.VISIBLE);
    }

    private void hideSocialToolBar() {

        socialToolBar.startAnimation(outAnim);
        socialToolBar.setVisibility(View.GONE);
    }

    public static ArrayList<String> getFilterTitles() {
        ArrayList<String> myFilterTitles = new ArrayList<String>();

        myFilterTitles.add("All Sites");
        myFilterTitles.add("WGU National");
        myFilterTitles.add("WGU Indiana");
        myFilterTitles.add("WGU Missouri");
        myFilterTitles.add("WGU Nevada");
        myFilterTitles.add("WGU Tennessee");
        myFilterTitles.add("WGU Texas");
        myFilterTitles.add("WGU Washington");

        return myFilterTitles;
    }

    public void setFilterTitle() {
        tvSocialDisplay.setText("Social: "+getFilterTitles().get(prefs.getSocialFilterNum()));
    }


    public void onEventMainThread(ESocialFilter event) {
        if (event == ESocialFilter.FILTER_CHANGE) {
            setFilterTitle();
            socialAdapter.refreshData();
        }
    }

    public void onEventMainThread(SocialItem item) {

        if (item.altUrl != null && item.altUrl.startsWith("fb://")) {
            try {
                int vc = getPackageManager().getPackageInfo("com.facebook.katana", 0).versionCode;
                Uri uri = Uri.parse(item.altUrl);
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
                return;
            } catch (Exception e) {
                // Do Nothing
            }
        }

        Uri webpage = Uri.parse(item.url);
        Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
        startActivity(webIntent);

    }

}
