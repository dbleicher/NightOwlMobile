package com.geofinity.wgu.nightowl.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.geofinity.pwnet.events.ECommunityMessages;
import com.geofinity.pwnet.events.ECoursesData;
import com.geofinity.pwnet.events.ECurrentTerm;
import com.geofinity.pwnet.events.EGradDate;
import com.geofinity.pwnet.events.EHCActions;
import com.geofinity.pwnet.events.EMentor;
import com.geofinity.pwnet.events.EProgTitle;
import com.geofinity.pwnet.events.EProgress;
import com.geofinity.pwnet.events.ERequests;
import com.geofinity.pwnet.events.EUnreadEmail;
import com.geofinity.pwnet.netops.RequestHelper;
import com.geofinity.wgu.nightowl.NOMApp;
import com.geofinity.wgu.nightowl.R;
import com.geofinity.wgu.nightowl.model.PocketPreferences;
import com.geofinity.wgu.nightowl.ui.adapters.HomeCardAdapter;
import com.geofinity.wgu.nightowl.ui.dialogs.DiagResources;
import com.geofinity.wgu.nightowl.ui.util.EMainFragments;
import com.geofinity.wgu.nightowl.ui.util.IMainActivity;
import com.geofinity.wgu.nightowl.ui.util.TargetedSwipeRefreshLayout;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import de.greenrobot.event.EventBus;

/**
 * Created by davidbleicher on 12/11/14.
 */
public class FragHome extends Fragment {
    private EventBus eBus;
    private PocketPreferences prefs;
    private IMainActivity myActivity;
    private RequestHelper reqHelp;

    private TargetedSwipeRefreshLayout swiper;
    private int columnCount;
    private RecyclerView sgv;
    private StaggeredGridLayoutManager sglm;
    private HomeCardAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // Get handle to static singletons
        eBus = EventBus.getDefault();
        prefs = NOMApp.prefs;
        reqHelp = NOMApp.reqHelp;

    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_home, container, false);

        //////////////////////////////
        //  Setup Swipe To Refresh  //
        //////////////////////////////
        swiper = (TargetedSwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        swiper.setSize(TargetedSwipeRefreshLayout.LARGE);
        swiper.setColorSchemeResources(
                R.color.wguGold,
                R.color.wguRedDark,
                R.color.wguPrimaryAccent,
                R.color.wguPrimaryDark
        );

        swiper.setOnRefreshListener(new TargetedSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reqHelp.refreshData(true);
            }
        });


        //////////////////////////////////////////////
        // Get number of columns and toolBar height //
        //////////////////////////////////////////////
        columnCount = getResources().getInteger(R.integer.home_sgv_column_count);

        //////////////////////////////////////////////
        //  Grab the StaggeredGrid & LayoutManager  //
        //////////////////////////////////////////////
        sgv = (RecyclerView) rootView.findViewById(R.id.rvHomeView);
        sgv.setHasFixedSize(true);

        sglm = new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        sgv.setLayoutManager(sglm);

        mAdapter = new HomeCardAdapter();
        sgv.setAdapter(mAdapter);

        // Set swiper's target!
        swiper.setTargetScrollableView(sgv);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.frag_home, menu);

        menu.findItem(R.id.goDegree).setIcon(
                new IconDrawable(getActivity().getApplicationContext(), FontAwesomeIcons.fa_graduation_cap)
                        .colorRes(R.color.wguWhite)
                        .sizeDp(20)
        );

        menu.findItem(R.id.goCommunity).setIcon(
                new IconDrawable(getActivity().getApplicationContext(), FontAwesomeIcons.fa_comments)
                        .colorRes(R.color.wguWhite)
                        .sizeDp(20)
        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.goDegree:
                myActivity.navigateToPage(EMainFragments.COURSES_FRAG);
                break;
            case R.id.goCommunity:
                myActivity.navigateToPage(EMainFragments.COMMUNITIES_FRAG);
                break;
        }
        return true;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Get handle to Activity Interface for
        // convenience calls to parent.
        myActivity = (IMainActivity) activity;
    }

    @Override
    public void onStart() {
        super.onStart();
        eBus.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        eBus.unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        
        swiper.setRefreshing(false);
    }

    /**
     * Handle actions spawned from the HCAdapter
     * @param action
     */
    public void onEventMainThread(EHCActions action) {
        switch (action) {
            case HC_LAUNCH_RESOURCES:
                // prefs.customToast(NOMApp.ac, "Resources are not yet implemented...", Toast.LENGTH_SHORT);
                DiagResources dRes = new DiagResources();
                dRes.show(getFragmentManager(), "resourceDiag");
                break;
            case HC_LAUNCH_SOCIAL:
                // prefs.customToast(NOMApp.ac, "Social is not yet implemented...", Toast.LENGTH_SHORT);
                startActivity(new Intent(getActivity().getApplicationContext(), ActSocial.class));
                break;
            case HC_LAUNCH_VIDEOLIST:
                startActivity(new Intent(getActivity().getApplicationContext(), ActVideoList.class));
                break;
            case HC_LAUNCH_COMMUNITY:
                myActivity.navigateToPage(EMainFragments.COMMUNITIES_FRAG);
                break;
            case HC_LAUNCH_WEBMAIL:
                Intent wmi = new Intent(getActivity(), ActSecWeb.class);
                wmi.putExtra("TARGET_URL", prefs.getWebmailUrl());
                wmi.putExtra("TARGET_TITLE", "WGU Webmail");
                startActivity(wmi);
                break;
            case HC_MENTOR_INFO:
                myActivity.showMentorInfo();
                break;
            case HC_EMAIL_MENTOR:
                myActivity.emailMyMentor();
                break;
            case HC_GOTO_COURSES:
                myActivity.navigateToPage(EMainFragments.COURSES_FRAG);
                break;
            case HC_GOTO_BADGE:
                startActivity(new Intent(getActivity(), ActBadge.class));
                break;
        }
    }


    /**
     * Handle events from the RequestHelper
     * @param event
     */
    public void onEventMainThread(ERequests event) {
        switch (event) {
            case REQUESTS_ACTIVE:
                swiper.setRefreshing(true);
                break;
            case REQUESTS_COMPLETE:
                swiper.setRefreshing(false);
                break;
        }
    }


    //////////////////////////////////////
    // Update cards on request success  //
    //////////////////////////////////////

    public void onEventMainThread(ECommunityMessages event) {
        if (event == ECommunityMessages.RETRIEVE_SUCCESS) {
            mAdapter.notifyItemChanged(mAdapter.HC_COMM_MAIL);
            mAdapter.notifyItemChanged(mAdapter.HC_UPDATED);
        }
    }

    public void onEventMainThread(EUnreadEmail event) {
        if (event == EUnreadEmail.RETRIEVE_SUCCESS) {
            mAdapter.notifyItemChanged(mAdapter.HC_COMM_MAIL);
            mAdapter.notifyItemChanged(mAdapter.HC_UPDATED);
        }
    }

    public void onEventMainThread(EProgress event) {
        if (event == EProgress.PROGRESS_SUCCESS) {
            mAdapter.notifyItemChanged(mAdapter.HC_PROGRAM);
            mAdapter.notifyItemChanged(mAdapter.HC_UPDATED);
        }
    }
    public void onEventMainThread(EProgTitle event) {
        if (event == EProgTitle.RETRIEVE_SUCCESS) {
            mAdapter.notifyItemChanged(mAdapter.HC_PROGRAM);
            mAdapter.notifyItemChanged(mAdapter.HC_UPDATED);
        }
    }
    public void onEventMainThread(ECurrentTerm event) {
        if (event == ECurrentTerm.RETRIEVE_SUCCESS) {
            mAdapter.notifyItemChanged(mAdapter.HC_PROGRAM);
            mAdapter.notifyItemChanged(mAdapter.HC_UPDATED);
        }
    }

    public void onEventMainThread(EMentor event) {
        if (event == EMentor.RETRIEVE_SUCCESS) {
            mAdapter.notifyItemChanged(mAdapter.HC_MENTOR);
            mAdapter.notifyItemChanged(mAdapter.HC_UPDATED);
        }
    }

    public void onEventMainThread(ECoursesData event) {
        if (event == ECoursesData.RETRIEVED_COURSE_DATA) {
            mAdapter.notifyItemChanged(mAdapter.HC_PROGRAM);
        }
    }

    public void onEventMainThread(EGradDate event) {
        if (event == EGradDate.RETRIEVE_SUCCESS) {
            mAdapter.notifyItemChanged(mAdapter.HC_PROGRAM);
        }
    }
}
