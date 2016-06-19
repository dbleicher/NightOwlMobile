package com.geofinity.wgu.nightowl.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.MailTo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;
import com.geofinity.pwnet.events.DownloaderEvent;
import com.geofinity.pwnet.events.ECos;
import com.geofinity.pwnet.events.ECourseMentors;
import com.geofinity.pwnet.events.ESForce;
import com.geofinity.pwnet.events.ETaskStream;
import com.geofinity.pwnet.models.jsonmodels.assessnew.Assessment;
import com.geofinity.pwnet.models.jsonmodels.assessnew.Attempt;
import com.geofinity.pwnet.models.jsonmodels.assessnew.JAssessDetails;
import com.geofinity.pwnet.models.jsonmodels.cosa.JActivity;
import com.geofinity.pwnet.models.jsonmodels.cosa.JStudyPlanA;
import com.geofinity.pwnet.models.jsonmodels.cosa.JSubject;
import com.geofinity.pwnet.models.jsonmodels.cosa.JTocEntry;
import com.geofinity.pwnet.models.jsonmodels.cosa.JTopic;
import com.geofinity.pwnet.models.jsonmodels.cosb.Competency;
import com.geofinity.pwnet.models.jsonmodels.cosb.CourseResourcesList;
import com.geofinity.pwnet.models.jsonmodels.cosb.CourseTools;
import com.geofinity.pwnet.models.jsonmodels.cosb.JStudyPlanB;
import com.geofinity.pwnet.models.jsonmodels.cosb.PamsAssessment;
import com.geofinity.pwnet.models.jsonmodels.cosb.PreparationsList;
import com.geofinity.pwnet.models.jsonmodels.degreeplanV6.JAssessment;
import com.geofinity.pwnet.models.jsonmodels.degreeplanV6.JCourse;
import com.geofinity.pwnet.netops.ReqAssessDetails;
import com.geofinity.pwnet.netops.ReqCOS;
import com.geofinity.pwnet.netops.ReqCOSBookmark;
import com.geofinity.pwnet.netops.ReqCOSChatter;
import com.geofinity.pwnet.netops.ReqCOSTipsAnns;
import com.geofinity.pwnet.netops.ReqCourseMentors;
import com.geofinity.pwnet.netops.ReqDownloadFile;
import com.geofinity.pwnet.netops.ReqPanopto;
import com.geofinity.pwnet.netops.ReqTSDeepLink;
import com.geofinity.wgu.nightowl.NOMApp;
import com.geofinity.wgu.nightowl.R;
import com.geofinity.wgu.nightowl.model.ECosNavActions;
import com.geofinity.wgu.nightowl.model.PocketPreferences;
import com.geofinity.wgu.nightowl.netops.CosProgressInterface;
import com.geofinity.wgu.nightowl.ui.adapters.CosNavDrawerAdapter;
import com.geofinity.wgu.nightowl.ui.dialogs.DiagCosToc;
import com.geofinity.wgu.nightowl.ui.util.TargetedSwipeRefreshLayout;
import com.google.gson.Gson;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.io.File;
import java.net.HttpCookie;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by davidbleicher on 9/11/14.
 * Updating for
 */
public class ActCoS extends ActionBarActivity {

    private JCourse myCourse;
    private MaterialDialog pd;
    private LinearLayout pbcSpin;

    private WebView wvContent;
    private LinearLayout llToolBar;
    private WebSettings settings;

    private EventBus eBus;
    private PocketPreferences prefs;

    private boolean showTOCMenu = false;
    private boolean detailsVisible = false;
    private MenuItem barToggle;

    private DrawerLayout dlCosNav;
    private ListView lvCosNav;
    private CosNavDrawerAdapter cosNavAdapter;

    private SwipeRefreshLayout swiper;

    private IconDrawable barToggleOn;
    private IconDrawable barToggleOff;

    private String pdfFilePath;
    private String cCode;

    private boolean clearBackAfterLoad = false;

    private String spBookmark;
    private boolean isCosA;
    private boolean isViewingAssessment;
    private JStudyPlanA sPlanA;
    private JStudyPlanB sPlanB;
    private ArrayList<JTocEntry> sPlanToc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.act_cos);

        eBus = EventBus.getDefault();
        eBus.register(this);
        prefs = NOMApp.prefs;

        pbcSpin = (LinearLayout) findViewById(R.id.pbcSpin);
        pbcSpin.setVisibility(View.GONE);

        // Make the spinner gold
        ProgressBar pbcSpin2 = (ProgressBar) findViewById(R.id.pbcSpin2);
        pbcSpin2.getIndeterminateDrawable().setColorFilter(0xFFF2C216, PorterDuff.Mode.SRC_IN);

        //////////////////////////////////////////
        //  Get the course out of the bundle    //
        //////////////////////////////////////////
        Bundle myExtras = getIntent().getExtras();
        if (myExtras != null) {
            Gson gson = new Gson();
            String mcJson = (String) myExtras.get("MY_COURSE");
            myCourse = gson.fromJson(mcJson, JCourse.class);
        }

        // Something went wrong!
        if (myCourse == null) {
            finish();
        }

        // For testing
        // Log.e("COS", myCourse.courseURL);
        // myCourse.courseCode = "C272";
        // myCourse.courseURL = "https://cos.wgu.edu/courses/for/C272";

        cCode = myCourse.courseCode;
        getSupportActionBar().setTitle(cCode);



        //////////////////////////////
        //  Setup The CosNav Menu   //
        //////////////////////////////
        barToggleOn = new IconDrawable(getApplicationContext(), FontAwesomeIcons.fa_toggle_on)
                .colorRes(R.color.wguWhite)
                .sizeDp(20);

        barToggleOff = new IconDrawable(getApplicationContext(), FontAwesomeIcons.fa_toggle_off)
                .colorRes(R.color.wguWhite)
                .sizeDp(20);

        dlCosNav = (DrawerLayout) findViewById(R.id.dlCosNav);

        lvCosNav = (ListView) findViewById(R.id.lvCosNav);
        cosNavAdapter = new CosNavDrawerAdapter(this, myCourse.title);
        lvCosNav.setAdapter(cosNavAdapter);

        lvCosNav.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CosNavDrawerAdapter.CosNavDrawerItem item = (CosNavDrawerAdapter.CosNavDrawerItem) cosNavAdapter.getItem(position);
                if (item.myType < 2) {
                    return;
                }
                performCosAction(item.myAction);
                dlCosNav.closeDrawer(Gravity.RIGHT);
            }
        });

        dlCosNav.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                if (barToggle != null) {
                    barToggle.setIcon(barToggleOn);
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                if (barToggle != null) {
                    barToggle.setIcon(barToggleOff);
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        // Set the File path to any downloaded COS PDF
        pdfFilePath = prefs.myExternalDir+"/"+ cCode +"_CourseOfStudy.pdf";

        //////////////////////////////
        //  Setup The WebView       //
        //////////////////////////////
        wvContent = (WebView) findViewById(R.id.wvSummary);

        settings = wvContent.getSettings();

        if (Build.MANUFACTURER.equalsIgnoreCase("Amazon")) {
            settings.setUserAgentString(prefs.UA_KINDLE_FIRE);
            // Log.i("COSWEB", "Pretending to be an Kindle Fire Tablet");
        }

        // settings.setUserAgentString(prefs.UA_IPHONE);

        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);  //Use zoom capability
        settings.setDisplayZoomControls(false); //Don't display the controls
        settings.setUseWideViewPort(false);
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        // Enable everything
        settings.setBlockNetworkLoads(false);
        settings.setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());
        settings.setAppCacheEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setGeolocationEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            settings.setAllowUniversalAccessFromFileURLs(true);
        }
        
        wvContent.setWebChromeClient(new WebChromeClient());
        wvContent.setWebViewClient(new MyCosViewClient());

        // Add CosProgress JavaScript Interface to webview
        wvContent.addJavascriptInterface(new CosProgressInterface(this), "CosProgress");

        ///////////////////////////////////////////////
        //  Add auth cookie to WebView cookie store  //
        ///////////////////////////////////////////////
        String loginToken = "";
        List<HttpCookie> clist = NOMApp.cookieMan.getCookieStore().getCookies();
        for (HttpCookie c : clist) {
            if (c.getName().contains("iPlanet")) {
                loginToken = c.getValue();
                break;
            }
        }
        final CookieManager cookieManager = CookieManager.getInstance();
        String cookieString = "iPlanetDirectoryPro=" + loginToken + "; domain=.wgu.edu";
        cookieManager.setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(wvContent, true);
        }
        cookieManager.setCookie("wgu.edu", cookieString);


        wvContent.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {

                String cookieString = CookieManager.getInstance().getCookie(url);

                // It's ok if cookieString is null, downloader checks for that
                // Log.e("DOWNCOOKIE", cookieString);
                NOMApp.opEx.add(new ReqDownloadFile(url, Uri.parse(url).getLastPathSegment(), cookieString));
            }
        });



        //////////////////////////////////
        //  Setup the progress dialog   //
        //////////////////////////////////
        pd = new MaterialDialog.Builder(this)
                .title("Loading Studyplan for " + cCode)
                .content(myCourse.title)
                .progress(true, 0)
                .build();

        //////////////////////////////
        //  Setup Swipe To Refresh  //
        //////////////////////////////
        swiper = (SwipeRefreshLayout) findViewById(R.id.srlContainer);
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
                if (isViewingAssessment) {
                    PocketPreferences.customToast(ActCoS.this, "Refeshing Assessment Details...", Toast.LENGTH_SHORT);
                    NOMApp.opEx.add(new ReqAssessDetails(myCourse.courseCode, String.valueOf(myCourse.courseVersionId)));
                } else {
                    PocketPreferences.customToast(ActCoS.this, "Refeshing Studyplan...", Toast.LENGTH_SHORT);
                    NOMApp.opEx.add(new ReqCOS(cCode, myCourse.courseUrl));
                }
            }
        });

        //////////////////////////
        //  Load the StudyPlan  //
        //////////////////////////
        spBookmark = prefs.getSPBookmark(myCourse.courseCode);
        if (spBookmark.contentEquals("")) {
            // We don't have the StudyPlan -- go fetch
            pd.show();
            NOMApp.opEx.add(new ReqCOS(cCode, myCourse.courseUrl));

        } else if (spBookmark.startsWith("cosa://")) {
            // Load CoS-A
            isCosA = true;
            eBus.post(ECos.COS_SPLAN_A_READY);

        } else {
            isCosA = false;
            Gson gson = new Gson();
            sPlanB = gson.fromJson(prefs.getStudyPlan(myCourse.courseCode), JStudyPlanB.class);
            eBus.post(ECos.COS_SPLAN_B_READY);
        }

    }

    @Override
    public void onBackPressed() {
        if (wvContent.canGoBack()) {
            wvContent.goBack();
        } else {
            eBus.post(ECos.COS_PAGE_READY);
        }
    }

    /**
     * Listener for File Download events (for the PDF)
     * @param event
     */
    public void onEventMainThread(DownloaderEvent event) {
        switch (event.status) {
            case DOWNLOADING:
                pbcSpin.setVisibility(View.VISIBLE);
                break;
            case STORAGE_PERMISSION_DENIED:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    NOMApp.reqHelp.nagAboutStorage(this);
                }
                prefs.customToast(this, "No permission to store files", Toast.LENGTH_LONG);
                break;
            case EXTERNAL_MEDIA_UNMOUNTED:
                prefs.customToast(this, "Cannot write to external directory.", Toast.LENGTH_LONG);
                pbcSpin.setVisibility(View.GONE);
                break;
            case DOWNLOAD_FAILED:
                prefs.customToast(this, "File Download Failed", Toast.LENGTH_LONG);
                pbcSpin.setVisibility(View.GONE);
                break;
            case DOWNLOAD_DONE:
                launchFileViewer(event.pathToFile);
                pbcSpin.setVisibility(View.GONE);
                break;
        }
    }


    /**
     * Listener for "events" from the CosToc Dialog.
     *
     * @param t (a JTocEntry
     */
    public void onEventMainThread(JTocEntry t) {
        Log.i("ACT_COS", "TOCEntry Event START");
        // pd.show();
        // NOMApp.opEx.add(new ReqCOS(cCode, t.myLink));
        spBookmark = t.cosaUrl;
        wvContent.loadDataWithBaseURL(
                "https://cos.wgu.edu/",
                formatCosAPage(),
                "text/html",
                "UTF-8",
                null);
    }


    /**
     * Listener for Course Mentor contact events
     * @param event
     */
    public void onEventMainThread(ECourseMentors event) {
        switch (event) {
            case COURSE_MENTORS_GETTING:
                pbcSpin.setVisibility(View.VISIBLE);
                break;
            case COURSE_MENTORS_DONE:
                pbcSpin.setVisibility(View.GONE);
                String mentorJSON = prefs.getCourseMentorJSON(myCourse.courseCode);
                if (mentorJSON.length() > 10) {
                    prefs.showMentorPage(this, mentorJSON);
                } else {
                    prefs.customToast(this, "Mentor contact data is unavailable.", Toast.LENGTH_LONG);
                }
                break;
            case COURSE_MENTORS_FAILED:
                pbcSpin.setVisibility(View.GONE);
                prefs.customToast(this, "Failed to get Course Mentors", Toast.LENGTH_SHORT);
                break;
        }
    }


    /**
     * Listener for SForce events
     *
     * @param event
     */
    public void onEventMainThread(ESForce event) {
        switch (event) {
            case SFORCE_FAILED:
                pbcSpin.setVisibility(View.GONE);
                prefs.customToast(this, "Failed to access SF.", Toast.LENGTH_SHORT);
                break;
            case SFORCE_AUTH_GETTING:
                pbcSpin.setVisibility(View.VISIBLE);
                break;
            case SFORCE_AUTH_DONE:
                pbcSpin.setVisibility(View.GONE);
                break;
            case SFORCE_COMMON_GETTING:
                pbcSpin.setVisibility(View.VISIBLE);
                break;
            case SFORCE_COMMON_DONE:
                pbcSpin.setVisibility(View.GONE);
                addSFItemsToNav();
                break;
            case SFORCE_NEWS_GETTING:
                pbcSpin.setVisibility(View.VISIBLE);
                break;
            case SFORCE_NEWS_DONE:
                pbcSpin.setVisibility(View.GONE);
                wvContent.loadDataWithBaseURL(
                        "https://srm.my.salesforce.com",
                        formatAnnouncements(),
                        "text/html",
                        "UTF-8",
                        null);
                clearBackAfterLoad = true;
                break;
            case SFORCE_TIPS_GETTING:
                pbcSpin.setVisibility(View.VISIBLE);
                break;
            case SFORCE_TIPS_DONE:
                pbcSpin.setVisibility(View.GONE);
                wvContent.loadDataWithBaseURL(
                        "https://srm.my.salesforce.com",
                        formatTips(),
                        "text/html",
                        "UTF-8",
                        null);
                clearBackAfterLoad = true;
                break;
            case SFORCE_CHATTER_GETTING:
                pbcSpin.setVisibility(View.VISIBLE);
                break;
            case SFORCE_CHATTER_DONE:
                pbcSpin.setVisibility(View.GONE);
                wvContent.loadDataWithBaseURL(
                        "https://srm.my.salesforce.com",
                        formatChatter(),
                        "text/html",
                        "UTF-8",
                        null);
                clearBackAfterLoad = true;
                break;
        }
    }


    public void onEventMainThread(ETaskStream event) {
        if (event.URLtoTry.startsWith("http")) {
            pd.dismiss();
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(event.URLtoTry));
            startActivity(i);
        } else {
            pd.setTitle("TaskStream Failed");
            pd.setContent(event.URLtoTry);
        }
    }

    /**
     * Listener for CoS Events
     *
     * @param event
     */
    public void onEventMainThread(ECos event) {
        Gson gson = new Gson();

        switch (event) {
            case COS_ASSDEETS_WORKED:
                if (isViewingAssessment) {
                    swiper.setRefreshing(false);
                    wvContent.loadDataWithBaseURL(
                            "https://cos.wgu.edu/",
                            formatAssDeets(),
                            "text/html",
                            "UTF-8",
                            null);
                }
                break;
            case COS_PROGUP_RETRIEVE:
                pd.setTitle("Updating Studyplan Progress");
                pd.show();
                break;
            case COS_PROGUP_WORKED:
                pd.dismiss();
                break;
            case COS_PROGUP_FAILED:
                pd.dismiss();
                new MaterialDialog.Builder(this)
                        .title("Progress Update Failed")
                        .content("Sorry, I couldn't reach the WGU servers to perform the progress update.")
                        .positiveText("OK")
                        .show();
                break;
            case COS_PAGE_READY:
                wvContent.loadDataWithBaseURL(
                        "https://cos.wgu.edu/",
                        (spBookmark.startsWith("cosa://")) ? formatCosAPage() : formatCosB(),
                        "text/html",
                        "UTF-8",
                        null);
                break;
            case COS_SPLAN_B_READY:
                Log.i("COS-PLANB", "Plan B Ready to Display");
                sPlanB = gson.fromJson(prefs.getStudyPlan(myCourse.courseCode), JStudyPlanB.class);
                spBookmark = prefs.getSPBookmark(myCourse.courseCode);
                // sPlanToc = sPlanB.getMyToc(spBookmark);
                // prefs.setCosToc(myCourse.courseCode, gson.toJson(sPlanToc));
                pd.dismiss();
                swiper.setRefreshing(false);
                wvContent.loadDataWithBaseURL(
                        "https://cos.wgu.edu/",
                        formatCosB(),
                        "text/html",
                        "UTF-8",
                        null);
                clearBackAfterLoad = true;

                // Enable the TOC menu item here.
                showTOCMenu = false;
                supportInvalidateOptionsMenu();

                // Add TaskStream Links, if appropriate
                addTaskStreamToNav();

                // Add Course Tools
                addSFItemsToNav();
                break;

            case COS_SPLAN_A_READY:
                sPlanA = gson.fromJson(prefs.getStudyPlan(myCourse.courseCode), JStudyPlanA.class);
                spBookmark = prefs.getSPBookmark(myCourse.courseCode);
                sPlanToc = sPlanA.getMyToc(spBookmark);
                prefs.setCosToc(myCourse.courseCode, gson.toJson(sPlanToc));
                pd.dismiss();
                swiper.setRefreshing(false);
                wvContent.loadDataWithBaseURL(
                        "https://cos.wgu.edu/",
                        formatCosAPage(),
                        "text/html",
                        "UTF-8",
                        null);
                clearBackAfterLoad = true;

                // Enable the TOC menu item here.
                showTOCMenu = true;
                supportInvalidateOptionsMenu();

                prefs.setCosPDFUrl(myCourse.courseCode,
                        "http://cospdf.wgu.edu/cos/cospdf.php?id="+sPlanA.studyPlanid);

                // Add PDF Link if we have one
                if(!prefs.getCosPDFUrl(cCode).equals("")) {
                    cosNavAdapter.addAction(ECosNavActions.COS_PDF);
                }

                // Add TaskStream Links, if appropriate
                addTaskStreamToNav();

                // Add Course Tools
                addSFItemsToNav();
                break;

            case COS_PAGE_FAILED:
                // Log.i("ACT_COS", "ECos Event Stop FAILED");
                // swiperCoS.setRefreshing(false);
                pd.dismiss();
                prefs.customToast(this, "FAILED LOADING COS PAGE", Toast.LENGTH_LONG);
                break;

            case COS_RETRIEVING:
                // Log.i("ACT_COS", "ECos Event Start RETRIEVING");
                // Using the Progress Dialog
                break;
        }

    }


    private class MyCosViewClient extends WebViewClient {
        boolean scaleChangedRunnablePending = false;

        @Override
        public void onScaleChanged(WebView view, float oldScale, float newScale) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (scaleChangedRunnablePending) return;
                view.postDelayed(new Runnable() {
                    @TargetApi(Build.VERSION_CODES.KITKAT)
                    @Override
                    public void run() {
                        wvContent.evaluateJavascript(
                                "document.body.style.width = window.innerWidth+'px';",
                                null
                        );
                        scaleChangedRunnablePending = false;
                    }
                }, 100);
            } else {
                super.onScaleChanged(view, oldScale, newScale);
            }
        }


        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            Log.e("ACT_COS", "SSLError: " + error.toString());
            handler.proceed();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            pbcSpin.setVisibility(View.GONE);

            if (clearBackAfterLoad) {
                wvContent.clearHistory();
                clearBackAfterLoad = false;
            }

            super.onPageFinished(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // Log.e("ACT_COS", "PageStart URL: "+url);
            pbcSpin.setVisibility(View.VISIBLE);
            super.onPageStarted(view, url, favicon);
        }


        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // Log.d("COSURL", url);

            if(url.startsWith("cosa://")) {
                spBookmark = url;
                eBus.post(ECos.COS_PAGE_READY);
                return true;
            }

            if (url.contains("youtu") || url.contains("//prezi")) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
                return true;
            }

            if (url.startsWith("pocketwguvid")) {
                Intent cvi = new Intent(getApplicationContext(), ActVideoPlayer.class);
                String delID = url.split("///")[1];
                cvi.putExtra("DELIVERY_ID", delID);
                startActivity(cvi);
                return true;
            }

            if (url.startsWith("pwguassess")) {
                performCosAction(ECosNavActions.ASSESS_DETAILS);
                return true;
            }

            if (url.contains("wgu.hosted.panopto.com")) {
                // NOMApp.prefs.setPanoptoFolderId(folderID);
                // NOMApp.prefs.setPanoptoFolderName(folderName);
                // NOMApp.opEx.add(new ReqPanopto(null, folderID, 24));

                // Link to Video session
                if (url.contains("folderID=")) {
                    try {
                        Intent cvi = new Intent(getApplicationContext(), ActVideoList.class);
                        String folderID = url.split("folderID=")[1];
                        prefs.setPanoptoFolderId(folderID);
                        prefs.setPanoSearchTerm(myCourse.courseCode+" videos");
                        startActivity(cvi);
                        NOMApp.opEx.add(new ReqPanopto(null, folderID, 24));
                    } catch (Exception e) {
                        Log.e("PANOVID", "Bad Folder Search: "+e.getMessage());
                    }
                    return true;
                }

                try {
                    Intent cvi = new Intent(getApplicationContext(), ActVideoPlayer.class);
                    String delID = url.split("id=")[1];
                    cvi.putExtra("DELIVERY_ID", delID);
                    startActivity(cvi);
                } catch (Exception e) {
                    Log.e("PANOVID", "Bad URL: "+url);
                }
                return true;
            }

            if (url.contains("pocketLoadSFChatter")) {
                NOMApp.opEx.add(new ReqCOSChatter(cCode));
                return true;
            }


            if (MailTo.isMailTo(url)) {
                MailTo mt = MailTo.parse(url);
                prefs.composeEmail(ActCoS.this, mt.getTo(), true);
                return true;
            }

            if (url.contains("cos.wgu.edu") && !url.contains("coachingreport")) {
                pd.show();
                NOMApp.opEx.add(new ReqCOS(cCode, url));
                return true;
            }

            if( !url.contains("//cos.wgu.edu/")
                    && !url.contains("//cosbservices.wgu.edu/")
                    && !url.contains("//cos2.wgu.edu/") ) {

                Intent wmi = new Intent(getApplicationContext(), ActSecWeb.class);
                wmi.putExtra("TARGET_URL", url);
                wmi.putExtra("TARGET_TITLE", "Learning Resource");
                startActivity(wmi);

                return true;
            }


            view.loadUrl(url);
            return false;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        eBus.unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Crashlytics.setString("CURRENT_LISTVIEW", this.getClass().getSimpleName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.act_cos, menu);

        MenuItem ct2 = menu.findItem(R.id.courseToc).setIcon(
                new IconDrawable(getApplicationContext(), FontAwesomeIcons.fa_list_alt)
                        .colorRes(R.color.wguWhite)
                        .sizeDp(20)
        );

        barToggle = menu.findItem(R.id.barToggle).setIcon(barToggleOff);

        // Hide when not available
        ct2.setVisible(showTOCMenu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // NavUtils.navigateUpTo(this, new Intent(this, ActMain.class));
                finish();
                break;

            case R.id.courseToc:
                eBus.post(ECos.COS_PAGE_READY);
                DiagCosToc cfl = new DiagCosToc();
                Bundle b = new Bundle();
                b.putString("COURSE_CODE", cCode);
                b.putString("COS_URL", spBookmark);
                cfl.setArguments(b);
                cfl.show(getSupportFragmentManager(), "coursefilterdiag");
                break;

            case R.id.barToggle:
                toogleToolbar();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toogleToolbar() {
        if (!dlCosNav.isDrawerOpen(GravityCompat.END)) {
            barToggle.setIcon(barToggleOn);
            dlCosNav.openDrawer(GravityCompat.END);
        } else {
            barToggle.setIcon(barToggleOff);
            dlCosNav.closeDrawer(GravityCompat.END);
        }
    }

    private void addSFItemsToNav() {
        if (cosNavAdapter.addHeader("Course Tools")) {
            dlCosNav.openDrawer(GravityCompat.END);
            cosNavAdapter.addAction(ECosNavActions.SF_CHATTER);
            cosNavAdapter.addAction(ECosNavActions.SF_NEWS);
            cosNavAdapter.addAction(ECosNavActions.SF_TIPS);
            // cosNavAdapter.addAction(ECosNavActions.SF_SEARCH);
            cosNavAdapter.notifyDataSetChanged();
        }
    }

    //////////////////////////////////////////////
    // Add TaskStream Nav Item if appropriate   //
    //////////////////////////////////////////////
    private void addTaskStreamToNav() {
        for (JAssessment a : myCourse.assessments) {
            if (a.type.equals("P")) {
                prefs.setTaskStreamUrl(cCode, a.title);
                cosNavAdapter.addHeader("TaskStream");
                cosNavAdapter.addTaskStream(a.title.substring(0,4));
            }
        }
    }

    private void performCosAction(ECosNavActions action) {

        String chatterJSON;
        long chatterLast;

        String tipsAnnsJSON;
        long taLast;

        switch (action) {
            case BACK_TO_COS:
                eBus.post(ECos.COS_PAGE_READY);
                break;
            case ASSESS_DETAILS:
                wvContent.loadDataWithBaseURL(
                        "https://cos.wgu.edu/",
//                        prefs.getCosAssessDetails(cCode),
                        formatAssDeets(),
                        "text/html",
                        "UTF-8",
                        null);
                clearBackAfterLoad = true;
                break;
            case COURSE_MENTORS:
                String mentorJSON = prefs.getCourseMentorJSON(myCourse.courseCode);
                if (mentorJSON.length() > 10) {
                    prefs.showMentorPage(this, mentorJSON);
                } else {
                    Log.i("MENTOR-C", "Need to go get the CM Group Email");
                    NOMApp.opEx.add(new ReqCourseMentors(myCourse.courseCode, myCourse.title));
                }
                break;
            case COURSE_VIDEOS:
                Intent cvi = new Intent(this, ActVideoList.class);
                cvi.putExtra("VIDEO_SEARCH", cCode ); // cCode or myCourse.courseName.trim()
                startActivity(cvi);
                break;
            case TASKSTREAM:
                pd.setTitle("Opening TaskStream");
                pd.show();
                for (JAssessment a : myCourse.assessments) {
                    if (a.type.equals("P")) {
                        NOMApp.opEx.add(new ReqTSDeepLink(a.title.substring(0,4), false));
                        break;
                    }
                }
                break;
            case TASKSTREAM_SCORE:
                pd.setTitle("Opening TaskStream");
                pd.show();
                for (JAssessment a : myCourse.assessments) {
                    if (a.type.equals("P")) {
                        NOMApp.opEx.add(new ReqTSDeepLink(a.title.substring(0,4), true));
                        break;
                    }
                }
                break;
            case COS_PDF:
                // String cpu = prefs.getCosPDFUrl(myCourse.courseCode);
                // Log.i("COS-PDF", "CPU: "+cpu);
                // wvContent.loadUrl(cpu);
                if (new File(pdfFilePath).exists()) {
                    launchFileViewer(pdfFilePath);
                } else {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                        NOMApp.reqHelp.nagAboutStorage(this);

                    } else {

                        NOMApp.opEx.add(
                                new ReqDownloadFile(
                                        prefs.getCosPDFUrl(cCode),
                                        cCode + "_CourseOfStudy.pdf", null //CookieManager.getInstance().getCookie("https://my.wgu.edu")
                                )
                        );
                    }
                }
                break;
            case SF_CHATTER:
                // Check for Chatter, Load if Exists
                // Run op if OLD, Run op if NOT exists
                chatterJSON = prefs.getChatterJSON(myCourse.courseCode);
                chatterLast = prefs.getChatterLast(myCourse.courseCode);
                if (chatterJSON.length() > 10) {
                    eBus.post(ESForce.SFORCE_CHATTER_DONE);
                    long ageMil = new Date().getTime() - chatterLast;
                    if (ageMil > 10 * 60 * 1000) {
                        // Refresh
                        NOMApp.opEx.add(new ReqCOSChatter(myCourse.courseCode));
                    }
                } else {
                    NOMApp.opEx.add(new ReqCOSChatter(myCourse.courseCode));
                }
                break;

            case SF_NEWS:
                // Check for TipsAnns, Load if Exists
                // Run op if OLD, Run op if NOT exists
                tipsAnnsJSON = prefs.getTipsAnnsJSON(myCourse.courseCode);
                taLast = prefs.getTipsAnnsLast(myCourse.courseCode);
                if (tipsAnnsJSON.length() > 10) {
                    eBus.post(ESForce.SFORCE_NEWS_DONE);
                    long ageMil = new Date().getTime() - taLast;
                    if (ageMil > 10 * 60 * 1000) {
                        // Refresh
                        NOMApp.opEx.add(new ReqCOSTipsAnns(myCourse.courseCode, ESForce.SFORCE_NEWS_GETTING));
                    }
                } else {
                    NOMApp.opEx.add(new ReqCOSTipsAnns(myCourse.courseCode, ESForce.SFORCE_NEWS_GETTING));
                }
                break;
            case SF_TIPS:
                // Check for TipsAnns, Load if Exists
                // Run op if OLD, Run op if NOT exists
                tipsAnnsJSON = prefs.getTipsAnnsJSON(myCourse.courseCode);
                taLast = prefs.getTipsAnnsLast(myCourse.courseCode);
                if (tipsAnnsJSON.length() > 10) {
                    eBus.post(ESForce.SFORCE_TIPS_DONE);
                    long ageMil = new Date().getTime() - taLast;
                    if (ageMil > 10 * 60 * 1000) {
                        // Refresh
                        NOMApp.opEx.add(new ReqCOSTipsAnns(myCourse.courseCode, ESForce.SFORCE_TIPS_GETTING));
                    }
                } else {
                    NOMApp.opEx.add(new ReqCOSTipsAnns(myCourse.courseCode, ESForce.SFORCE_TIPS_GETTING));
                }
                break;
            case SF_SEARCH:
                clearBackAfterLoad = true;
                break;
        }
    }


    private void launchFileViewer(String filePath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);

        intent.setDataAndType(Uri.fromFile(new File(filePath)),
                URLConnection.guessContentTypeFromName(filePath));

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        try {
            startActivity(intent);
        }
        catch (Exception e) {
            e.printStackTrace();
            prefs.customToast(this, "No Application Available to View: "+filePath, Toast.LENGTH_LONG);
        }
    }

    private int getBookmarkTocIndex(ArrayList<JTocEntry> tocList) {
        for (JTocEntry t : tocList) {
            if (t.isBookmark) return tocList.indexOf(t);
        }
        return 0;
    }


    private String rewriteVideoFrames (String htmlSnippet) {

        Document doc = Jsoup.parse(htmlSnippet);


        //////////////////////////////////////
        // Rewrite any YouTube Video links  //
        //////////////////////////////////////
        Elements tubes = doc.select("iframe[src*=youtu]");
        for (Element t : tubes) {
            Element vidLink = new Element(Tag.valueOf("a"), "");
            Element vidImg = new Element(Tag.valueOf("img"), "");

            String vl = t.attr("src");
            vl = vl.replaceFirst("httpss", "https");


            String vi = "https://img.youtube.com/vi/" +
                    vl.split("embed/")[1].split("\\?")[0] +
                    "/0.jpg";

            vidLink.attr("href", vl);
            vidImg.attr("src", vi);
            vidImg.attr("width", "320");
            vidImg.attr("height", "240");

            vidLink.appendChild(vidImg);
            t.replaceWith(vidLink);
        }


        //////////////////////////////////////
        // Rewrite any Panopto Video links  //
        //////////////////////////////////////
        Elements panos = doc.select("iframe[src*=panopto]");
        for (Element p : panos) {

            // Log.e("PANO_ELEMENT", p.outerHtml());

            try {
                // Get the SessionID
                String code = p.attr("src").split("&")[0].split("=")[1];

                // Log.e("PANO_VID", code);

                // DO STUFF HERE
                String tURL = "https://panopto-a.akamaihd.net/sessions/d45b469a-57cd-4a74-92ae-97d01e643bbb/350e2607-7fea-475c-af66-3691c4451a46_et/thumbs/slide0.jpg";
                String sURL = "pocketwguvid:///"+code;

                Element video = new Element(Tag.valueOf("a"), "");
                Element thumb = new Element(Tag.valueOf("img"), "");
                thumb.attr("width", "240");
                thumb.attr("height", "180");
                thumb.attr("src", tURL);

                video.attr("href", sURL);
                video.appendChild(thumb);

                p.replaceWith(video);
                // System.out.println("PanoTag: "+video.outerHtml());
            } catch (Exception ve) {
                // Nothing
            }
        }

        return doc.outerHtml();

    }


    private String formatCosAPage () {
        isViewingAssessment = false;
        StringBuilder sb = new StringBuilder();

        String[] bParts = spBookmark.split("/");
        long rId = Long.valueOf(bParts[bParts.length - 1]);
        String rKind = bParts[bParts.length - 2];

        ArrayList<JTocEntry> tocList = sPlanA.getMyToc(spBookmark);
        prefs.setSPBookmark(myCourse.courseCode, spBookmark);
        // Good place to update the server bookmark!
        NOMApp.opEx.add(new ReqCOSBookmark(rKind, sPlanA.studyPlanid, rId));

        int curDex = getBookmarkTocIndex(tocList);
        String prevLink = "&nbsp;";
        String nextLink = "&nbsp;";
        if (curDex > 0) {
            prevLink = String.format(
                    "<input type='button' value='&lt; Prev' class='cpnBut' onClick=\"window.location='%s'\" />",
                    tocList.get(curDex - 1).cosaUrl);
        }
        if (curDex < (tocList.size() - 1)) {
            nextLink = String.format(
                    "<input type='button' value='Next &gt;' class='cpnBut' onClick=\"window.location='%s'\" />",
                    tocList.get(curDex + 1).cosaUrl);
        }

        String cProg = "...";
        if (sPlanA.totalActivities > 0) {
            cProg = String.format(
                    "%.0f%% Complete - %d/%d Activities",
                    (((float)sPlanA.doneActivities / sPlanA.totalActivities) * 100),
                    sPlanA.doneActivities,
                    sPlanA.totalActivities
            );
        }

        // Log.i("FDP-COS", cProg);

        String hNav = String.format(
                "%s <span class='pocketCPS'>%s</span> %s <hr />",
                prevLink,
                cProg,
                nextLink);


        sb.append(prefs.cosHead);
        if (rKind.contentEquals("intro")) {
            sb.append("<div class='activity-right-top'>");
            sb.append(hNav);
            sb.append("</div>");
            sb.append("<h2>Introduction</h2>");
            sb.append(rewriteVideoFrames(sPlanA.introduction));

        } else if (rKind.contentEquals("subject")) {
            for (JSubject sub : sPlanA.subjects) {
                if (sub.id == rId) {
                    sb.append("<div class='activity-right-top'>");
                    sb.append(hNav);
                    sb.append("</div>");
                    sb.append(String.format("<h2>%s</h2>", sub.title));
                    sb.append(String.format("%s", rewriteVideoFrames(sub.shortDescription)));
                    break;
                }
            }
        } else if (rKind.contentEquals("topic")){
            boolean tFound = false;
            for (JSubject sub : sPlanA.subjects) {
                for (JTopic topic : sub.topics) {
                    if (topic.id == rId) {
                        sb.append("<div class='activity-right-top'>");
                        sb.append(hNav);
                        sb.append("</div>");
                        sb.append(String.format("<h2>%s</h2>", topic.title));
                        sb.append(String.format("%s", rewriteVideoFrames(topic.instruction)));

                        int[] tac = topic.actCounts();
                        if (tac[2] != 0) {
                            // This topic has activities
                            String bv = "Topic Incomplete";
                            String bc = "cpnBut";
                            int ts = 0;
                            if (tac[2] == (tac[0] + tac[1])) {
                                bv = "Topic Completed";
                                bc = "cosProgressDone";
                                ts = 2;
                            }
                            String statBut = String.format(
                                    "<input type='button' value='%s' class='%s' onClick=\"cosProgressChoose(%d, '%s', '%s', %d, %d, %d, %d, %d);\" />",
                                    bv,
                                    bc,
                                    ts,
                                    myCourse.courseCode,
                                    "topic",
                                    sPlanA.studyPlanid,
                                    sub.id,
                                    topic.id,
                                    0,
                                    0
                            );
                            sb.append("<div class='activity-right-top'>");
                            sb.append(statBut);
                            sb.append("</div>");
                            sb.append("<p />");
                        }

                        for (JActivity act : topic.activities) {
                            sb.append("<div class='activity-content'>");
                            sb.append(String.format("<h3>%s</h3>", act.title));
                            sb.append(String.format("%s", rewriteVideoFrames(act.instruction)));

                            String bv = "Activity Incomplete";
                            String bc = "cpnBut";
                            int as = 0;
                            if (act.isComplete) {
                                bv = "Activity Complete";
                                bc = "cosProgressDone";
                                as = 2;
                            } else if (act.isSkipped) {
                                bv = "Activity Skipped";
                                bc = "cosProgressDone";
                                as = 1;
                            }

                            // arTaskButton.attr("onClick", "cosProgressChoose(1, '"+courseCode+"', '"+myPath+"', '"+currentURL+"')");

                            String statBut = String.format(
                                    "<input type='button' value='%s' class='%s' onClick=\"cosProgressChoose(%d, '%s', '%s', %d, %d, %d, %d, %d);\" />",
                                    bv,
                                    bc,
                                    as,
                                    myCourse.courseCode,
                                    "activity",
                                    sPlanA.studyPlanid,
                                    sub.id,
                                    topic.id,
                                    act.id,
                                    act.userActivityId
                                    );

                            sb.append("<div class='activity-right'>");
                            sb.append(statBut);
                            sb.append("</div>");
                            sb.append("<p />");
                            sb.append("</div>");
                        }

                        tFound = true;
                        break;
                    }
                }
                if (tFound) break;
            }
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    public String formatCosB() {
        isViewingAssessment = false;
        // sPlanB should be populated

        // Build the basic HTML page
        StringBuilder cosDoc = new StringBuilder();
        cosDoc.append(prefs.cosHead);

        // Add the title and intro
        if (sPlanB.courseTitle.startsWith("- "+sPlanB.pamsCode+" - ")) {
            cosDoc.append(String.format("<h2>%s</h2>", sPlanB.courseTitle.substring(8)));
        } else {
            cosDoc.append(String.format("<h2>%s</h2>", sPlanB.courseTitle));
        }
        cosDoc.append(sPlanB.courseIntro);

        // Add Course Materials Stuff
        cosDoc.append("<h2>Learning Resource</h2>");
        if (sPlanB.vendorLongDescription != null) {
            cosDoc.append(sPlanB.vendorLongDescription);
        } else if (sPlanB.vendorShortDescription != null) {
            cosDoc.append(sPlanB.vendorShortDescription);
        }

        String statBut = String.format(
                "<input type='button' value='Go to Course Material' class='cosProgressDone' onClick=\"window.location='%s'\" />",
                sPlanB.courseURL
        );

        cosDoc.append("<div class='activity-right'>");
        cosDoc.append(statBut);
        cosDoc.append("</div>");
        cosDoc.append("<p />");

        // Add PAMS Assessment List
        if (sPlanB.pamsAssessments.size() > 0) {
            cosDoc.append("<div class='activity-content'>");
            cosDoc.append("<h3>Assessments</h3>");
            cosDoc.append("<ul>");
            for (PamsAssessment pas : sPlanB.pamsAssessments) {
                cosDoc.append(String.format("<li><a href='pwguassess://assessments'>%s</a> (%s)</li>",
                        pas.title,
                        pas.bannerCode
                ));
            }
            cosDoc.append("</ul>");
            cosDoc.append("</div>");
            cosDoc.append("<p />");
        }

        // Grab the courseTools Object
        CourseTools cts = sPlanB.courseTools;

        // Deal with Course Resource List
        if (cts != null && cts.CourseResourcesList != null) {
            cosDoc.append("<div class='activity-content'>");
            cosDoc.append("<h3>Course Resources</h3>");
            cosDoc.append("<ul>");
            for (CourseResourcesList crl : cts.CourseResourcesList) {
                cosDoc.append(String.format("<li><a href='%s'>%s</a></li>",
                        crl.src,
                        crl.title
                ));
            }
            cosDoc.append("</ul>");
            cosDoc.append("</div>");
            cosDoc.append("<p />");
        }

        // Deal with Preparations List
        if (cts != null && cts.PreparationsList != null) {
            cosDoc.append("<div class='activity-content'>");
            cosDoc.append("<h3>Additional Preparation Resources</h3>");
            cosDoc.append("<ul>");
            for (PreparationsList prl : cts.PreparationsList) {
                cosDoc.append(String.format("<li><a href='%s'>%s</a></li>",
                        prl.src,
                        prl.title
                ));
            }
            cosDoc.append("</ul>");
            cosDoc.append("</div>");
            cosDoc.append("<p />");
        }


        // Add Competencies
        if (sPlanB.competencies.size() > 0) {
            cosDoc.append("<div class='activity-content'>");
            cosDoc.append("<h3>Competencies</h3>");
            cosDoc.append("<ul>");
            for (Competency comp : sPlanB.competencies) {
                cosDoc.append(String.format("<li><b>%s</b>: %s</li>",
                        comp.title,
                        comp.description
                ));
            }
            cosDoc.append("</ul>");
            cosDoc.append("</div>");
        }

        // Close the HTML page
        cosDoc.append("</body></html>");

        return cosDoc.toString();
    }

    public String formatChatter() {
        String badChat = String.format(
                "<html><head>%s</head><h3>No Current %s Chatter</h3><body></html>",
                prefs.cssSFStyles,
                myCourse.courseCode);

        try {
            JSONObject chatObj = new JSONObject(prefs.getChatterJSON(myCourse.courseCode));
            JSONArray postsList = chatObj.optJSONArray("posts");

            if (postsList != null && postsList.length() > 0) {
                int numPosts = postsList.length();
                StringBuilder sb = new StringBuilder();
                sb.append(String.format(
                        "<html><head>%s</head><div class='activity-right'><h3>%s Course Chatter</h3></div>",
                        prefs.cssSFStyles,
                        myCourse.courseCode));

                JSONObject post;
                for (int x=0; x < numPosts; x++) {
                    post = postsList.optJSONObject(x);
                    if (!post.getString("deletedStatus").contains("null")) continue; // Deleted Post
                    JSONObject postUser = post.optJSONObject("user");

                    sb.append("<div class='activity-content'>");
                        sb.append(String.format("<div class='%s'>", (postUser.getBoolean("student")) ? "person" : "person--mentor"));
                            sb.append("<div class='person__avatar'><img height='28' width='28' src='https://srm.my.salesforce.com/img/userprofile/default_profile_45.png' /></div>");
                            sb.append("<div class='person__content'>");
                                sb.append(String.format("<p class='person__name'>%s %s.</p>", postUser.getString("firstName"), postUser.getString("lastInitial")));
                                sb.append(String.format(
                                        "<p class='person__date'><span class='person__type'>%s: </span>%s</p>",
                                        post.getString("type"),
                                        NOMApp.sdfDisplay.format(NOMApp.sdfChatterIn.parse(post.getString("createdDate")))
                                ));
                                sb.append(String.format("<p class='person__message'>%s</p>", post.getString("body")));
                            sb.append("</div>");
                        sb.append("</div>");

                        // Do the Comments Stuff Here
                        // sb.append(String.format(""));
                        if (post.optJSONArray("comments") != null) {
                            JSONArray comments = post.getJSONArray("comments");
                            int numComms = comments.length();
                            JSONObject cmt;
                            for (int y = 0; y < numComms; y++) {
                                cmt = comments.getJSONObject(y);
                                if (!cmt.optString("deletedStatus").contains("null")) continue; // Deleted Comment
                                JSONObject cmtUser = cmt.getJSONObject("user");
                                sb.append(String.format("<div class='%s' style='margin-left:40px;'>", (cmtUser.getBoolean("student")) ? "person" : "person--mentor"));
                                    sb.append("<div class='person__avatar'><img height='28' width='28' src='https://srm.my.salesforce.com/img/userprofile/default_profile_45.png' /></div>");
                                    sb.append("<div class='person__content'>");
                                        sb.append(String.format("<p class='person__name'>%s %s.</p>", cmtUser.getString("firstName"), cmtUser.getString("lastInitial")));
                                        sb.append(String.format(
                                                "<p class='person__date'><span class='person__type'>%s: </span>%s</p>",
                                                cmt.getString("type"),
                                                NOMApp.sdfDisplay.format(NOMApp.sdfChatterIn.parse(cmt.getString("createdDate")))
                                        ));
                                        sb.append(String.format("<p class='person__message'>%s</p>", cmt.getString("body")));
                                    sb.append("</div>");
                                sb.append("</div>");
                            }
                        }
                    sb.append("</div><p/>");
                }
                sb.append("</body></html>");
                return sb.toString();
            }
        } catch (Exception e) {
            Log.e("CHATTER", e.getMessage());
            return badChat;
        }
        return badChat;
    }


    private class Ann implements Comparable<Ann> {
        public String announcement;
        public String announcementDate;

        public Ann (String announcement, String announcementDate) {
            this.announcement = announcement;
            this.announcementDate = announcementDate;
        }

        @Override
        public int compareTo(@NonNull Ann another) {
            return another.announcementDate.compareTo(this.announcementDate);
        }

    }

    public String formatAnnouncements() {
        String badAnns = String.format(
                "<html><head>%s</head><h3>No Current %s Announcements</h3><body></html>",
                prefs.cssSFStyles,
                myCourse.courseCode);
        try {
            JSONObject taObj = new JSONObject(prefs.getTipsAnnsJSON(myCourse.courseCode));
            JSONArray annsList = taObj.getJSONArray("announcementList");
            if (annsList != null && annsList.length() > 0) {
                int numAnns = annsList.length();
                StringBuilder sb = new StringBuilder();
                sb.append(String.format(
                        "<html><head>%s</head><div class='activity-right'><h3>%s Course Announcements</h3></div>",
                        prefs.cssSFStyles,
                        myCourse.courseCode));

                ArrayList<Ann> annie = new ArrayList<Ann>();
                for (int x=0; x < numAnns; x++) {
                    JSONObject ann = annsList.getJSONObject(x);
                    annie.add(new Ann(ann.getString("announcement"), ann.getString("announcementDate").trim()));
                }
                Collections.sort(annie);

                for (Ann a : annie) {
                    sb.append("<div class='activity-content'>");
                    sb.append(String.format("<h3>%s</h3>", a.announcementDate));
                    sb.append(String.format("%s", a.announcement));
                    sb.append("</div><p/>");
                }

                sb.append("</body></html>");
                return sb.toString();
            }
        } catch (Exception e) {
            return badAnns;
        }
        return badAnns;
    }

    public String formatTips() {
        String badTips = String.format(
                "<html><head>%s</head><h3>No Current %s Tips</h3><body></html>",
                prefs.cssSFStyles,
                myCourse.courseCode);

        try {
            JSONObject taObj = new JSONObject(prefs.getTipsAnnsJSON(myCourse.courseCode));
            JSONArray tipsList = taObj.getJSONArray("tipList");
            if (tipsList != null && tipsList.length() > 0) {
                int numTips = tipsList.length();
                StringBuilder sb = new StringBuilder();
                sb.append(String.format(
                        "<html><head>%s</head><div class='activity-right'><h3>%s Course Tips</h3></div>",
                        prefs.cssSFStyles,
                        myCourse.courseCode));

                for (int x=0; x < numTips; x++) {
                    JSONObject tip = tipsList.getJSONObject(x);
                    sb.append("<div class='activity-content'>");
                    // sb.append(String.format("<h3>Date: %s</h3>", tip.getString("expirationDate")));
                    sb.append(String.format("%s", tip.getString("tip")));
                    sb.append("</div><p/>");
                }
                sb.append("</body></html>");
                return sb.toString();
            }
        } catch (Exception e) {
            return badTips;
        }

        return badTips;
    }

    public String formatAssDeets() {
        isViewingAssessment = true;

        Gson gson = new Gson();
        JAssessDetails assDeets = gson.fromJson(
                prefs.getAssessDetails(myCourse.courseCode),
                JAssessDetails.class);

        // Build the basic HTML page
        StringBuilder cosDoc = new StringBuilder();
        cosDoc.append(prefs.cosHead);

        for (Assessment ass : assDeets.assessment) {
            cosDoc.append("<div>");
            cosDoc.append(String.format("<h2>%s - %s</h2>", ass.assessmentCode, ass.assessmentTitle));
            if (ass.assessmentSubType != null) {
                cosDoc.append(String.format("<span><b>%s</b></span>", ass.assessmentSubType));
            } else if (ass.assessmentType != null) {
                cosDoc.append(String.format("<span><b>%s</b></span>", ass.assessmentType));
            }

            if (ass.history != null) {
                cosDoc.append("<ul>");
                cosDoc.append(String.format(
                        "<li>Enrolled: %s</li>",
                        (ass.history.enrolled) ? "Yes" : "No"
                ));
                cosDoc.append(String.format(
                        "<li>Attempts: %d of %d allowed</li>",
                        ass.history.numberOfAttempts,
                        (ass.history.numberOfAttempts + ass.history.remainingAttempts)
                ));

                if (ass.cutScorePercentage != null) {
                    cosDoc.append(String.format(
                            "<li>Cut Score: %s%%</li>",
                            ass.cutScorePercentage
                    ));
                }
                if (ass.numberOfItems != null) {
                    cosDoc.append(String.format(
                            "<li>Questions: %s</li>",
                            ass.numberOfItems
                    ));
                }
                if (ass.timeAllotedInMinutes != null) {
                    cosDoc.append(String.format(
                            "<li>Duration: %s minutes</li>",
                            ass.timeAllotedInMinutes
                    ));
                }
                cosDoc.append("</ul>");
            }

            if (ass.history.attempts != null && ass.history.attempts.size() > 0) {
                for (Attempt oneAtt : ass.history.attempts) {
                    cosDoc.append("<div class='activity-content' style='background-color:#F6F6F6;'>");
                    cosDoc.append(String.format("<h3>Attempt #%d:</h3>", oneAtt.attemptNumber));
                    cosDoc.append("<ul>");
                    if (oneAtt.status != null) {
                        cosDoc.append(String.format("<li>Status: %s</li>", oneAtt.status));
                    } else if (oneAtt.referred) {
                        cosDoc.append("<li>Status: In Progress</li>");
                    } else if (oneAtt.approved) {
                        cosDoc.append("<li>Status: Approved</li>");
                    }

                    if(oneAtt.score != null) {
                        cosDoc.append(String.format("<li>Score: %s%%</li>", oneAtt.score));
                    }

                    if (oneAtt.resultDateStr != null) {
                        cosDoc.append(String.format("<li>Result Date: %s</li>", oneAtt.resultDateStr));
                    } else if (oneAtt.referredDateStr != null) {
                        cosDoc.append(String.format("<li>Referral Date: %s</li>", oneAtt.referredDateStr));
                    }

                    if (oneAtt.coachingReportURL != null) {
                        cosDoc.append(String.format("<li><a href='%s'>Coaching Report</a></li>", oneAtt.coachingReportURL));
                    }

                    cosDoc.append("</ul>");
                    cosDoc.append("</div>");
                    cosDoc.append("<p />");
                }
            }

            cosDoc.append("</div><hr class='cfade' />");
            cosDoc.append("<p />");
        }

        cosDoc.append("</body></html>");

        return cosDoc.toString();
    }


}
