package com.geofinity.wgu.nightowl.ui;

import android.Manifest;
import android.app.DownloadManager;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.geofinity.pwnet.events.EPanoptoVideo;
import com.geofinity.pwnet.models.PanoVideo;
import com.geofinity.pwnet.netops.ReqPanopto;
import com.geofinity.wgu.nightowl.NOMApp;
import com.geofinity.wgu.nightowl.R;
import com.geofinity.wgu.nightowl.model.PocketPreferences;
import com.geofinity.wgu.nightowl.ui.adapters.VideosAdapter;
import com.geofinity.wgu.nightowl.ui.util.QRBarDecoration;
import com.geofinity.wgu.nightowl.ui.util.TargetedSwipeRefreshLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.joanzapata.iconify.widget.IconButton;
import com.squareup.okhttp.OkHttpClient;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by davidbleicher on 8/31/14.
 *
 * Done (b213): implement video download
 * TODO: fix bug in "Show More" when viewing Folder contents.
 * TODO: add "back" button when selecting a folder
 * Done: fix bug in videolist first run (no content)
 * Done (b213): fix bug where "progress" not displayed while loading (measure before refresh)
 */
public class ActVideoList extends ActionBarActivity {

    private TargetedSwipeRefreshLayout swiper;
    private RecyclerView sgv;
    private StaggeredGridLayoutManager sglm;

    private LinearLayout videoToolBar;
    private TextView tvVideoDisplay;
    private IconButton itvClear;
    private Button itvMore;

    private MenuItem searchItem;

    private EventBus eBus;
    private VideosAdapter vidsAdapter;
    private ArrayList<PanoVideo> myDataset;
    private Type vidListType;

    PocketPreferences prefs;

    private int columnCount;
    private int qrBarHeight;

    private Animation inAnim;
    private Animation outAnim;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.act_video_listing);

        prefs = NOMApp.prefs;
        eBus = EventBus.getDefault();
        vidListType = new TypeToken<List<PanoVideo>>(){}.getType();

        //////////////////////////
        //  Setup The Toolbar   //
        //////////////////////////
        inAnim  = AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_top);
        outAnim = AnimationUtils.loadAnimation(this, R.anim.abc_slide_out_top);
        inAnim.setDuration(600);
        outAnim.setDuration(600);

        videoToolBar = (LinearLayout) findViewById(R.id.videoToolBar);
        tvVideoDisplay = (TextView) findViewById(R.id.tvVideoDisplay);
        tvVideoDisplay.setText("Refreshing...");

        itvClear = (IconButton) findViewById(R.id.itvClear);
        itvMore = (Button) findViewById(R.id.itvMore);

        itvClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.setPanoSearchTerm("");
                prefs.setPanoptoFolderId("");
                prefs.setPanoptoFolderName("");
                itvClear.setVisibility(View.GONE);
                NOMApp.opEx.add(new ReqPanopto(null, null, 24));
            }
        });

        itvMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int showMore = prefs.getPanoptoResultCount()+24;
                NOMApp.opEx.add(new ReqPanopto(prefs.getPanoSearchTerm(), prefs.getPanoptoFolderId(), showMore));
            }
        });

        // Setup the toolBar's text/button display
        formatResults();


        //////////////////////////////
        //  Setup Swipe To Refresh  //
        //////////////////////////////
        swiper = (TargetedSwipeRefreshLayout) findViewById(R.id.swipe_container);
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
                prefs.customToast(ActVideoList.this, "Refreshing Video List...", Toast.LENGTH_SHORT);
                NOMApp.opEx.add(new ReqPanopto(
                        prefs.getPanoSearchTerm(),
                        null,
                        prefs.getPanoptoResultCount()
                ));
            }
        });


        //////////////////////////////////////////////
        // Get number of columns and toolBar height //
        //////////////////////////////////////////////
        columnCount = getResources().getInteger(R.integer.sgv_column_count);
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
            qrBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }

        //////////////////////////////////////////////
        //  Grab the StaggeredGrid & LayoutManager  //
        //////////////////////////////////////////////
        sgv = (RecyclerView) findViewById(R.id.rvVideoList);
        sgv.addItemDecoration(new QRBarDecoration(columnCount, qrBarHeight));
        sgv.setHasFixedSize(true);

        sglm = new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        sgv.setLayoutManager(sglm);


        //////////////////////////////
        //  Setup Adapter & DataSet //
        //////////////////////////////
        myDataset = new ArrayList<PanoVideo>();
        try {
            Gson gson = new Gson();
            myDataset = gson.fromJson(NOMApp.prefs.getPanoContent(), vidListType);
        } catch (Exception e) {
            Log.e("PANO_LOAD", "Puked Loading Pano from Prefs: " + e.getMessage());
        }
        // Log.e("VIDS", "MyDataset has: "+myDataset.size());

        vidsAdapter = new VideosAdapter(this, myDataset);

        sgv.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                if (dy > 3) {
                    if (videoToolBar.getVisibility() == View.VISIBLE)
                        hideVideoToolBar();

                } else if (dy < -3) {

                    if (videoToolBar.getVisibility() == View.GONE)
                        showVideoToolBar();
                }
            }
        });

        sgv.setAdapter(vidsAdapter);

        // Set swiper's target!
        swiper.setTargetScrollableView(sgv);


        // Perform a measure to "wake up" the swiper in onCreate()
        swiper.measure(1,1);

        // Refresh only if we really need to.
        if (savedInstanceState != null) {
            // Just an orientation change
            refreshIfOlder();
        } else {
            // Actual activity creation
            Bundle myExtras = getIntent().getExtras();
            String searchFor = null;
            if (myExtras != null) {
                searchFor = (String) myExtras.get("VIDEO_SEARCH");
            }
            if (searchFor != null && !searchFor.equals(prefs.getPanoSearchTerm())) {
                swiper.setRefreshing(true);
                prefs.setPanoSearchTerm(searchFor);
                NOMApp.opEx.add(new ReqPanopto(searchFor, null, 24));
            } else {
                refreshIfOlder();
            }
        }

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


    public void onEventMainThread(EPanoptoVideo event) {
        switch (event) {
            case SEARCH_STARTED:
                swiper.setRefreshing(true);
                tvVideoDisplay.setText("Retrieving...");
                break;
            case SEARCH_FAILED:
                swiper.setRefreshing(false);
                tvVideoDisplay.setText("Search Failed");
                break;
            case SEARCH_COMPLETED:
                swiper.setRefreshing(false);
                vidsAdapter.refreshDisplay();
                formatResults();
                sgv.scrollToPosition(0);
                videoToolBar.setVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     * Event arrives when the user has requested a video download.
     *
     * @param vid
     */
    public void onEventMainThread(final PanoVideo vid) {
        // The first thing we should do is ask the user
        // if he/she is sure.

        //AlertDialog.Builder adb = new AlertDialog.Builder(this);
        //adb.setMessage("\""+Html.fromHtml(vid.getSessionName())+"\"");
        //adb.setTitle("Download Video?");
        //
        //adb.setPositiveButton("Download", new DialogInterface.OnClickListener() {
        //    @Override
        //    public void onClick(DialogInterface dialog, int which) {
        //        new DownloadPanoVideo().execute(vid);
        //    }
        //});
        //adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        //    @Override
        //    public void onClick(DialogInterface dialog, int which) {
        //        NOMApp.prefs.customToast(ActVideoList.this, "Canceling download...", Toast.LENGTH_SHORT);
        //    }
        //});
        //
        //adb.create().show();

        // Let's pester the user for Storage permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            NOMApp.reqHelp.nagAboutStorage(this);

        } else {

            new MaterialDialog.Builder(this)
                    .title("Download Video?")
                    .content("\"" + Html.fromHtml(vid.getSessionName()) + "\"")
                    .positiveText("Download")
                    .negativeText("Cancel")
                    .positiveColorRes(R.color.wguPrimary)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            new DownloadPanoVideo().execute(vid);
                        }

                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            NOMApp.prefs.customToast(ActVideoList.this, "Canceling download...", Toast.LENGTH_SHORT);
                        }
                    })
                    .show();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.frag_video, menu);

        searchItem = menu.findItem(R.id.video_frag_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint("Search for...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // handle search here
                swiper.setRefreshing(true);
                prefs.setPanoSearchTerm(query);
                NOMApp.opEx.add(new ReqPanopto(query, null, 24));
                MenuItemCompat.collapseActionView(searchItem);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // NavUtils.navigateUpTo(this, new Intent(this, ActMain.class));
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void showVideoToolBar() {

        videoToolBar.startAnimation(inAnim);
        videoToolBar.setVisibility(View.VISIBLE);
    }

    private void hideVideoToolBar() {

        videoToolBar.startAnimation(outAnim);
        videoToolBar.setVisibility(View.GONE);
    }

    public void refreshIfOlder () {
        swiper.setRefreshing(true);
        tvVideoDisplay.setText("Retrieving...");
        long rfInterval = (2 * 60 * 60 * 1000); // 2 Hours in Milliseconds

        long now = new Date().getTime();
        long lastRF = prefs.getPanoLastFetch();

        if ((now - lastRF) > rfInterval ) {
            NOMApp.opEx.add(new ReqPanopto(
                    prefs.getPanoSearchTerm(),
                    prefs.getPanoptoFolderId(),
                    prefs.getPanoptoResultCount()));
        } else {
            formatResults();
            swiper.setRefreshing(false);
        }
    }

    public void formatResults() {
        if (prefs.getPanoSearchTerm().equals("") && prefs.getPanoptoFolderName().equals("")) {
            tvVideoDisplay.setText("Latest WGU Videos");
            itvClear.setVisibility(View.GONE);
            itvMore.setVisibility(View.GONE);

        } else if (!prefs.getPanoptoFolderName().equals("")) {
            itvClear.setVisibility(View.VISIBLE);
            int totVids = prefs.getPanoptoTotalHits();
            int shownVids = prefs.getPanoptoResultCount();
            if (totVids > shownVids) {
                tvVideoDisplay.setText(
                        String.format("Folder: \"%s\"\nShowing %d of %d videos",
                                prefs.getPanoptoFolderName(),
                                shownVids,
                                totVids)
                );
                itvMore.setVisibility(View.VISIBLE);
            } else {
                tvVideoDisplay.setText(
                        String.format("Folder: \"%s\"\nContains %d videos",
                                prefs.getPanoptoFolderName(),
                                totVids)
                );
                itvMore.setVisibility(View.GONE);
            }

        } else {
            itvClear.setVisibility(View.VISIBLE);
            int totVids = prefs.getPanoptoTotalHits();
            int shownVids = prefs.getPanoptoResultCount();
            if (totVids > shownVids) {
                tvVideoDisplay.setText(
                        String.format("Search for: \"%s\"\nShowing %d of %d videos",
                                prefs.getPanoSearchTerm(),
                                shownVids,
                                totVids)
                );
                itvMore.setVisibility(View.VISIBLE);
            } else {
                tvVideoDisplay.setText(
                        String.format("Search for: \"%s\"\nFound %d videos",
                                prefs.getPanoSearchTerm(),
                                totVids)
                );
                itvMore.setVisibility(View.GONE);
            }
        }

    }

    public class DownloadPanoVideo extends AsyncTask<PanoVideo, Void, Void> {
        private final OkHttpClient okClient = NOMApp.nlm.getOkClient();

        PanoVideo vid;
        DownloadManager downMan;
        DownloadManager.Request downReq;

        String streamURL = null;
        String targetFile = null;
        String fullDownPath = null;
        String sessionTitle = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (streamURL != null && fullDownPath != null) {

                new MaterialDialog.Builder(ActVideoList.this)
                        .title("Video is Downloading...")
                        .content("The video is downloading in the background and will be saved to " +
                                "your \"My Saved Files\" section when it's done.")
                        .positiveText("OK")
                        .positiveColorRes(R.color.wguPrimary)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {

                            }
                        })
                        .show();

                downMan = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

                downReq = new DownloadManager.Request(Uri.parse(streamURL))
                        .setDescription("Downloading WGU Video")
                        .setTitle("Video: \""+sessionTitle+"\"")
                        .setMimeType("video/mp4")
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setDestinationUri(Uri.parse(fullDownPath));

                downMan.enqueue(downReq);
            }
        }

        @Override
        protected Void doInBackground(PanoVideo... params) {
            vid = params[0];
            sessionTitle = Html.fromHtml(vid.getSessionName()).toString();
            streamURL = NOMApp.reqHelp.getPanoptoStreamUrl(vid.getDeliveryID());
            targetFile = sessionTitle.replaceAll("\\W+", "_")+".mp4";
            fullDownPath = "file://"+prefs.myExternalDir+"/"+targetFile;
            return null;
        }
    }

}
