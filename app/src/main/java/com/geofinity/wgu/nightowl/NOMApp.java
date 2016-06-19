package com.geofinity.wgu.nightowl;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.crashlytics.android.Crashlytics;
import com.geofinity.pwnet.NetLibMain;
import com.geofinity.pwnet.netops.OpsExecutor;
import com.geofinity.pwnet.netops.RequestHelper;
import com.geofinity.wgu.nightowl.model.PocketPreferences;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.squareup.okhttp.OkHttpClient;

import java.net.CookieManager;
import java.text.SimpleDateFormat;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;

/**
 * Created by davidbleicher on 8/9/14.
 *
 * --Before Releasing App--
 * Done b211: 1. Improve ToolBar in Community & Courses (Sort)
 * Done b211: 2. Improve CommMess & Course Cells for size and content
 * Done b211: 3. Improve FragHome display (RecyclerView?)
 * Done b211: 4. Implement Resources
 * Done b211: 5. Implement Social
 * Done b211: 6. Implement Progress Badge
 * Done b211: 7. Implement proper "About" page
 * Done b213: 8. Implement "My Downloads" activity
 * Done b213: 9. Fix the scrolling / swiperefresh bugs in RV/SGLM
 * Done 220: Style all dialogs consistently
 *
 * Other Stuff:
 * In build 240 (2.3.0)
 * Done: Fix Student / Course Mentor Display
 * Done: Add student "Signature" to wgu-bound emails
 * Done: Eliminate SalesForce, use new API for Chatter, Tips, Announcements
 * Done: Add check for "Offline" mode (Network Connectivity).
 * Done: Open TaskStream in External Browser
 * Done: Restore DP for inactive students, fix progress
 * Done: Use DP as source of progress reporting
 * Done: Sort Tips & Announcements
 * Done: Add link to myWGU
 *
 * Build 241 (2.3.1)
 * DONE: Fix alignment of Accountability badge on Nex6P
 * DONE: Add prompt for WRITE permission to Badge "Save"
 * DONE: Figure out the FDP data load issue for 2 students
 * DONE: Fix dialing of Mentor phone numbers

 * Build 243 (2.3.3)
 * Done: more fixes to DP processing, more diagnostics
 * Done: implement "Sign Out"
 * Done: optimize program title and grad date
 * Done: optimize PIDM retrieval
 *
 * Build 245 (2.3.5)
 * Done: Add TaskStream Score/Queue link
 * Done: Rejigger FDP timeout exception handling
 * TODO: Add Cohorts processing
 *
 * TODO: Add TaskStream Button to Assessments
 * TODO: Add Competencies button to Assessments
 * TODO: Add custom per-course Progress Badgeswpoinde
 * TODO: Consider adding "News" to home page (from Portal or Blog?)
 * TODO (MR2): Create an app Widget (Progress, Course, Messages list)
 *
 */
public class NOMApp extends Application {

    private static NOMApp sInstance;
    private static OkHttpClient okClient;

    public static NetLibMain nlm;
    public static OpsExecutor opEx;
    public static RequestHelper reqHelp;

    private static int shortVer = 0;
    private static String longVer = "";
    private static long lastAppUpdate;

    public static Context ac;
    public static PocketPreferences prefs;

    public static SimpleDateFormat sdfDisplay;
    public static SimpleDateFormat sdfIngest;
    public static SimpleDateFormat sdfShort;
    public static SimpleDateFormat sdfNice;
    public static SimpleDateFormat sdfYMD;

    public static SimpleDateFormat sdfShiftDTIn;
    public static SimpleDateFormat sdfShiftDTOut;

    public static SimpleDateFormat sdfChatterIn;

    public static CookieManager cookieMan;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        ac = getApplicationContext();

        nlm = new NetLibMain(ac);

        Iconify.with(new FontAwesomeModule());

        sInstance = this;
        opEx = OpsExecutor.getInstance();
        // Log.e("NOMApp", "Thread Pool Size: "+opEx.getPoolSize());

        sdfDisplay = new SimpleDateFormat("MMM d, yyyy 'at' h:mma", Locale.getDefault());
        sdfIngest = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyyy", Locale.getDefault() );
        sdfNice = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault() );
        sdfShort = new SimpleDateFormat("MM/dd/yy", Locale.getDefault() );
        sdfYMD = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault() );

        sdfShiftDTIn = new SimpleDateFormat("HHmm Z", Locale.getDefault() );
        sdfShiftDTOut = new SimpleDateFormat("h:mm a", Locale.getDefault() );

        sdfChatterIn = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.000'Z", Locale.getDefault() );

        PackageInfo pInfo;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
            shortVer = pInfo.versionCode;
            longVer = pInfo.versionName;
            lastAppUpdate = pInfo.lastUpdateTime;
        } catch (PackageManager.NameNotFoundException e1) { }

        // Create and get handle to app-wide singletons
        prefs = PocketPreferences.getInstance(ac);
        reqHelp = RequestHelper.getInstance(ac);
        cookieMan = nlm.cookieMan;
    }

    public synchronized static NOMApp getInstance() {
        return sInstance;
    }

    // public static WebkitCookieManagerProxy getCookieMan() { return coreCookieManager; }

    public static int getShortVer() {
        return shortVer;
    }

    public static String getLongVer() {
        return longVer;
    }

    public static long getLastAppUpdate() {
        return lastAppUpdate;
    }


}
