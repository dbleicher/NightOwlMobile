package com.geofinity.wgu.nightowl.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.geofinity.pwnet.events.ECoursesData;
import com.geofinity.pwnet.events.ERequests;
import com.geofinity.wgu.nightowl.NOMApp;
import com.geofinity.wgu.nightowl.R;
import com.geofinity.wgu.nightowl.ui.adapters.CourseListAdapter;
import com.geofinity.wgu.nightowl.ui.dialogs.DiagCourseFilter;
import com.geofinity.wgu.nightowl.ui.dialogs.DiagCourseSort;
import com.geofinity.wgu.nightowl.ui.util.EMainFragments;
import com.geofinity.wgu.nightowl.ui.util.IMainActivity;
import com.geofinity.wgu.nightowl.ui.util.QRBarDecoration;
import com.geofinity.wgu.nightowl.ui.util.TargetedSwipeRefreshLayout;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.widget.IconButton;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

//import android.support.v7.widget.RecyclerView;

/**
 * Created by davidbleicher on 8/20/14.
 *
 * Done: fix bug where filter/sort is lost on refresh/rotate
 *
 */
public class FragCourses extends Fragment {

    private TargetedSwipeRefreshLayout swiper;
    private RecyclerView sgv;
    private StaggeredGridLayoutManager sglm;

    private LinearLayout courseToolBar;
    private TextView tvCourseFilter;
    private IconButton ibFilter;
    private IconButton ibSort;

    // private MenuItem searchItem;

    private IMainActivity myActivity;
    private EventBus eBus;
    private CourseListAdapter cmAdapter;

    private int columnCount;
    private int qrBarHeight;

    private Animation inAnim;
    private Animation outAnim;

    int[] fcvips = {5,5,5};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        eBus = EventBus.getDefault();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_courses, container, false);

        //////////////////////////////
        //  Setup Course Toolbar    //
        //////////////////////////////
        inAnim  = AnimationUtils.loadAnimation(getActivity(), R.anim.abc_slide_in_top);
        outAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.abc_slide_out_top);
        inAnim.setDuration(600);
        outAnim.setDuration(600);

        courseToolBar = (LinearLayout) rootView.findViewById(R.id.courseToolBar);

        tvCourseFilter = (TextView) rootView.findViewById(R.id.tvCourseFilter);
        ibFilter = (IconButton) rootView.findViewById(R.id.ibFilter);
        ibSort   = (IconButton) rootView.findViewById(R.id.ibSort);

        ibFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DiagCourseFilter cfl = new DiagCourseFilter();
                cfl.show(getFragmentManager(), "coursefilterdialog");
            }
        });
        ibSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DiagCourseSort cfl = new DiagCourseSort();
                cfl.show(getFragmentManager(), "coursesortdialog");
            }
        });
        setToolbarTitle();


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
                NOMApp.prefs.customToast(getActivity(), "Refeshing Courses...", Toast.LENGTH_SHORT);
                NOMApp.reqHelp.performCoursesDataRefresh();
            }
        });

        //////////////////////////////////////////////
        // Get number of columns and toolBar height //
        //////////////////////////////////////////////
        columnCount = getResources().getInteger(R.integer.sgv_column_count);
        TypedValue tv = new TypedValue();
        if (getActivity().getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
            qrBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }

        //////////////////////////////////////////////
        //  Grab the StaggeredGrid & LayoutManager  //
        //////////////////////////////////////////////
        sgv = (RecyclerView) rootView.findViewById(R.id.rvCourseList);
        sgv.addItemDecoration(new QRBarDecoration(columnCount, qrBarHeight));
        sgv.setHasFixedSize(true);

        sglm = new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        sgv.setLayoutManager(sglm);

        //////////////////////////////
        //  Setup Adapter & DataSet //
        //////////////////////////////
        cmAdapter = new CourseListAdapter( getActivity() );


        //////////////////////////////
        //  Setup Scroll Listener   //
        //////////////////////////////
        sgv.setOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 3) {
                    if (courseToolBar.getVisibility() == View.VISIBLE)
                        hideCommToolBar();

                } else if (dy < -3) {

                    if (courseToolBar.getVisibility() == View.GONE)
                        showCommToolBar();
                }
            }
        });

        sgv.setAdapter(cmAdapter);

        // Set swiper's target!
        swiper.setTargetScrollableView(sgv);

        return rootView;
    }


    public void onEventMainThread(ECoursesData event) {
        switch (event) {
            case NO_COURSE_VERSION_ID:
                new MaterialDialog.Builder(getActivity())
                        .title("Studyplan Not Available")
                        .content(
                                "WGU did not provide a course Studyplan for this account.  "
                                        +"Course content is only available for actively enrolled "
                                        +"WGU students.  It is not available before the start "
                                        +"of your first term, while on a 'Term Break', or after graduation."
                        )
                        .positiveText("OK")
                        .show();
                break;
            case GETTING_COURSE_DATA:
                swiper.setRefreshing(true);
                break;
            case RETRIEVED_COURSE_DATA:
                swiper.setRefreshing(true);
                cmAdapter.refreshDisplay();
                setToolbarTitle();
                swiper.setRefreshing(false);
                break;
            case FAILED_PROFILE_DATA:
                swiper.setRefreshing(false);
                break;
            case FAILED_DP_DATA:
                swiper.setRefreshing(false);
                break;
            case FAILED_DP_SERVER:
                swiper.setRefreshing(false);
                break;
            case FAILED_DP_NETWORK:
                swiper.setRefreshing(false);
                break;
        }

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.frag_courses, menu);

        menu.findItem(R.id.goHome).setIcon(
                new IconDrawable(getActivity().getApplicationContext(), FontAwesomeIcons.fa_home)
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
            case R.id.goHome:
                myActivity.navigateToPage(EMainFragments.HOME_FRAG);
                break;
            case R.id.goCommunity:
                myActivity.navigateToPage(EMainFragments.COMMUNITIES_FRAG);
                break;
        }
        return true;
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
                cmAdapter.refreshDisplay();
                break;
        }
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

    private void showCommToolBar() {
        courseToolBar.startAnimation(inAnim);
        courseToolBar.setVisibility(View.VISIBLE);
    }

    private void hideCommToolBar() {
        courseToolBar.startAnimation(outAnim);
        courseToolBar.setVisibility(View.GONE);
    }


    public static ArrayList<String> getFilterTitles() {
        ArrayList<String> myFilterTitles = new ArrayList<String>();

        myFilterTitles.add("All Terms & Courses");
        myFilterTitles.add("Current & Future Terms");
        myFilterTitles.add("Current Term Only");
        myFilterTitles.add("Previous Terms Only");

        myFilterTitles.add("Status: Enrolled");
        myFilterTitles.add("Status: Passed");
        myFilterTitles.add("Status: Not Attempted");
        myFilterTitles.add("Status: Not Passed");

        return myFilterTitles;
    }

    public static ArrayList<String> getSortingTitles() {
        ArrayList<String> mySortingTitles = new ArrayList<String>();

        mySortingTitles.add("Standard");
        mySortingTitles.add("Date Ascending");
        mySortingTitles.add("Date Descending");
        mySortingTitles.add("Code Ascending");
        mySortingTitles.add("Code Descending");
        mySortingTitles.add("Name Ascending");
        mySortingTitles.add("Name Descending");

        return mySortingTitles;
    }

    private void setToolbarTitle() {
        ArrayList<String> myFilterTitles = getFilterTitles();
        ArrayList<String> mySortingTitles = getSortingTitles();

        int cfNum = NOMApp.prefs.getCourseFilterNum();
        int csNum = NOMApp.prefs.getCourseSortingNum();

        if (cfNum < myFilterTitles.size() && csNum < mySortingTitles.size()) {
            tvCourseFilter.setText(myFilterTitles.get(cfNum)+"\nSorting: "+mySortingTitles.get(csNum));
        } else {
            tvCourseFilter.setText(myFilterTitles.get(0));
        }
    }

}
