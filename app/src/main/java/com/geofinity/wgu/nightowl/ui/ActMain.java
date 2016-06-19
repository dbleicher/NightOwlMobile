package com.geofinity.wgu.nightowl.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.geofinity.pwnet.events.EAuth;
import com.geofinity.pwnet.events.ECampusNews;
import com.geofinity.pwnet.events.ECoursesData;
import com.geofinity.pwnet.netops.RequestHelper;
import com.geofinity.wgu.nightowl.NOMApp;
import com.geofinity.wgu.nightowl.R;
import com.geofinity.wgu.nightowl.model.PocketPreferences;
import com.geofinity.wgu.nightowl.ui.adapters.MainPagerAdapter;
import com.geofinity.wgu.nightowl.ui.dialogs.DiagAbout;
import com.geofinity.wgu.nightowl.ui.dialogs.DiagSettings;
import com.geofinity.wgu.nightowl.ui.util.EMainFragments;
import com.geofinity.wgu.nightowl.ui.util.IMainActivity;

import de.greenrobot.event.EventBus;


public class ActMain extends ActionBarActivity
        implements FragNavigationDrawer.NavigationDrawerCallbacks, IMainActivity {

    private PocketPreferences prefs;
    private EventBus eBus;
    private RequestHelper reqHelp;

    private ViewPager mainPager;
    private MainPagerAdapter mainPagerAdapter;
    private String[] actions;

    private FragNavigationDrawer mFragNavigationDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);

        prefs = NOMApp.prefs;
        reqHelp = NOMApp.reqHelp;
        eBus = EventBus.getDefault();

        //////////////////////////
        // Setup the ActionBar  //
        //////////////////////////
        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowHomeEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.actionbar_custom, null);

        //////////////////////////////
        // Add Spinner to ActionBar //
        //////////////////////////////
        ab.setCustomView(mCustomView);
        ab.setDisplayShowCustomEnabled(true);

        //////////////////////////////////
        // Setup the Navigation Drawer  //
        //////////////////////////////////
        mFragNavigationDrawer = (FragNavigationDrawer) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mFragNavigationDrawer.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        //////////////////////////////////////
        // Setup the ViewPager and pages    //
        //////////////////////////////////////
        mainPager = (ViewPager) findViewById(R.id.vpMainPager);
        mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        mainPager.setAdapter(mainPagerAdapter);
        mainPager.setOffscreenPageLimit(1);

        mainPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        setTitle("PocketWGU");
                        break;
                    case 1:
                        setTitle("My Courses");
                        break;
                    case 2:
                        setTitle("Community");
                        break;
                }
            }

            @Override
            public void onPageScrolled(int i, float v, int i2) {
                //
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                //
            }
        });
        
        ////////////////////////////////////////////////
        // This is a trick to make all indeterminate
        // progressBars (spinning circle) use the accent
        // color, app-wide, on Android 4.x
        //////////////////////////////////////////////
        TypedArray a;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            a = getTheme().obtainStyledAttributes(new int[]{android.R.attr.colorAccent});
        } else {
            a = getTheme().obtainStyledAttributes(new int[]{R.attr.colorAccent});
        }
        new ProgressBar(this)
                .getIndeterminateDrawable()
                .setColorFilter(a.getColor(0,0), PorterDuff.Mode.SRC_ATOP);
        

        //////////////////////////
        // Authenticate & Go    //
        //////////////////////////
        authenticateIfNecessary();

        // Log.i("UA", System.getProperty("http.agent"));

    }


    //////////////////////////////////////////////////
    // Deal with the Nav Drawer and AB restoration  //
    //////////////////////////////////////////////////
    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Log.i("MAINPAGER", "Called onNavigationDrawerItemSelected");

        switch (position) {
            case 0:
                // Launch Webmail
                Intent wmi = new Intent(this, ActSecWeb.class);
                wmi.putExtra("TARGET_URL", prefs.getWebmailUrl());
                wmi.putExtra("TARGET_TITLE", "WGU Webmail");
                startActivity(wmi);
                break;
            case 1:
                // Progress Badge
                // navigateToPage(EMainFragments.HOME_FRAG);
                // eBus.post(EProgressBadge.REQUESTED);
                startActivity(new Intent(this, ActBadge.class));
                break;
            case 2:
                // Settings
                DiagSettings ds = new DiagSettings();
                ds.show(getSupportFragmentManager(), "settingsDialog");
                break;
            case 3:
                // My Saved Files
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    NOMApp.reqHelp.nagAboutStorage(this);

                } else {
                    startActivity(new Intent(this, ActMySavedFiles.class));
                }
                break;
            case 4:
                // Refresh
                new MaterialDialog.Builder(this)
                        .title("Sign Out")
                        .content(
                                "You may sign in again, exit PocketWGU, or " +
                                        "clear all account information before exiting."
                        )
                        .cancelable(false)
                        .positiveText("Sign In Again")
                        .neutralText("Exit PocketWGU")
                        .negativeText("Clear My Account")
                        .negativeColor(ContextCompat.getColor(ActMain.this, R.color.wguRedDark))
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {
                                DiagSettings ds = new DiagSettings();
                                ds.show(getSupportFragmentManager(), "settingsDialog");
                                return;
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {
                                NOMApp.nlm.prefs.resetAll(ActMain.this, false);
                                finish();
                                System.exit(0);
                            }
                        })
                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {
                                finish();
                                System.exit(0);
                            }
                        })
                        .show();

                // prefs.resetAll(this, true);
                // reqHelp.refreshData(true);
                break;
            case 6:
                // Call Mentor
                showMentorInfo();
                break;
            case 7:
                // Email Mentor
                emailMyMentor();
                break;
            case 9:
                // About PocketWGU
                DiagAbout diagAbout = new DiagAbout();
                diagAbout.show(getSupportFragmentManager(), "aboutDialog");
                // startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://pocketwgu.geofinity.com/aboutpw.php?v="+NOMApp.getShortVer())));
                break;
            case 10:
                // Send Feedback
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("message/rfc822");
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"pocketwgu@gmail.com"});
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "My Feedback on PocketWGU "+NOMApp.getLongVer());
                startActivity(Intent.createChooser(emailIntent, "Email PocketWGU Feedback..."));
                break;
            default:
                break;
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        eBus.unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        eBus.register(this);
    }

    /**
     * Listen for EAuth events, maybe refresh data
     */
    public void onEventMainThread(EAuth event) {
        // Log.e("ACTMAIN", "EAuth: "+event);
        switch (event) {
            case AUTH_WORKED:
                reqHelp.politeRefreshCommMailCourse();
                break;
            case AUTH_NET_OFFLINE:
                new MaterialDialog.Builder(this)
                        .title("Offline Mode")
                        .content("Could not reach WGU via the network.  " +
                                "Currently working in 'Offline Mode'.  Content in 'My Saved Files' " +
                                "and any previously viewed Studyplans are available.  Other features " +
                                "requiring network connectivity will not function.")
                        .positiveText("OK")
                        .build()
                        .show();
                break;
            case AUTH_FAILED:
                if (getSupportFragmentManager().findFragmentByTag("settingsDialog") == null) {
                    DiagSettings ds = new DiagSettings();
                    ds.show(getSupportFragmentManager(), "settingsDialog");
                }
                break;
        }
    }


    public void onEventMainThread(ECoursesData event) {
        switch (event) {
            case FAILED_DP_DATA:
                prefs.customToast(this, "Empty Degree Plan", Toast.LENGTH_LONG);
//                new MaterialDialog.Builder(this)
//                        .title("Empty Degree Plan")
//                        .content(
//                                "Sorry, I was unable to retrieve your degree plan.\n\n"
//                                        +"Please check your network connection and try refreshing again.  If the problem persists, let us "
//                                        +"know using the 'Send Us Feedback' option in the main menu.  Thanks!"
//                        )
//                        .positiveText("OK")
//                        .show();
                break;
            case FAILED_DP_NETWORK:
                prefs.customToast(this, "Network error getting degree plan.\n" +
                        "Please check your connection and try again.", Toast.LENGTH_LONG);
//                new MaterialDialog.Builder(this)
//                        .title("Network Timeout")
//                        .content(
//                                "Sorry, there was a network error while accessing your degree plan.\n\n"
//                                        +"Please check your network connection and try refreshing again.  If the problem persists, let us "
//                                        +"know using the 'Send Us Feedback' option in the main menu.  Thanks!"
//                        )
//                        .positiveText("OK")
//                        .show();
                break;
            case FAILED_DP_SERVER:
                prefs.customToast(this, "Server error getting degree plan.\n" +
                        "Please check your connection and try again.", Toast.LENGTH_LONG);
//                new MaterialDialog.Builder(this)
//                        .title("Server Error")
//                        .content(
//                                "Sorry, there was a server error while accessing your degree plan.\n\n"
//                                        +"Please check your network connection and try refreshing again.  If the problem persists, let us "
//                                        +"know using the 'Send Us Feedback' option in the main menu.  Thanks!"
//                        )
//                        .positiveText("OK")
//                        .show();
                break;
            case FAILED_PROFILE_DATA:
                prefs.customToast(this, "Error retrieving account profile.\n" +
                        "Please check your connection and try again.", Toast.LENGTH_LONG);
//                new MaterialDialog.Builder(this)
//                        .title("Failed Getting Account Profile")
//                        .content(
//                                "Sorry, I was unable to retrieve your account's profile.\n\n"
//                                        +"Please check your network connection and try refreshing again.  If the problem persists, let us "
//                                        +"know using the 'Send Us Feedback' option in the main menu.  Thanks!"
//                        )
//                        .positiveText("OK")
//                        .show();
                break;
        }
    }


    /**
     * Listen for ECampusNews events
     */
    public void onEventMainThread(ECampusNews event) {
        // Log.e("ACTMAIN", "EAuth: "+event);
        switch (event) {
            case CAMPUS_NEWS_READY:
                // Launch SecWeb with URL
                Intent wmi = new Intent(this, ActSecWeb.class);
                wmi.putExtra("TARGET_URL", "campusNews://");
                wmi.putExtra("TARGET_TITLE", "Campus News");
                startActivity(wmi);
                break;
            case CAMPUS_NEWS_FAILED:
                Log.e("CAMPUS-NEWS", "Failed");
                break;
        }
    }


    @Override
    public void showMentorInfo() {

        String smJSON = prefs.getStudentMentorJSON();
        if (smJSON.length() < 10) return;
        Log.i("MENTOR-S", smJSON);
        prefs.showMentorPage(this, smJSON);
    }

    @Override
    public void emailMyMentor() {
        prefs.composeEmail(this, prefs.getMentorEmail(), true);
    }


    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        Log.i("MAIN_AB", "ActionBar Restore Called!");
    }


    //////////////////////////////////////////////
    // Implement the IMainActivity Interface    //
    //////////////////////////////////////////////

    @Override
    public void navigateToPage(EMainFragments frag) {
        switch (frag) {
            case HOME_FRAG:
                mainPager.setCurrentItem(0);
                break;
            case COURSES_FRAG:
                mainPager.setCurrentItem(1);
                break;
            case COMMUNITIES_FRAG:
                mainPager.setCurrentItem(2);
                break;
            default:
                mainPager.setCurrentItem(0);
        }
    }

    @Override
    public ActionBar getMyActionBar() {
        return getSupportActionBar();
    }

    @Override
    public void authenticateIfNecessary() {

        // If no credentials, open the Settings Dialog
        if (prefs.isFirsRun() || prefs.getUserName().equals("") || prefs.getUserPass().equals("")) {
            // There are no credentials
            DiagSettings ds = new DiagSettings();
            ds.show(getSupportFragmentManager(), "settingsDialog");
            return;
        }

        //Determine if we need to authenticate
        if (reqHelp.shouldSSOAuthenticate()){

            // I need to authenticate
            reqHelp.ssoAuthenticate();
        }
    }


    @Override
    public String displayCommFilter() {
        String ccn = prefs.getCurrentCommName();
        String ccs = prefs.getCommSearchCurrent();

        if (ccs.equals("")) {
            return ccn;
        } else {
            return String.format("Search for \"%s\" in %s", ccs, ccn);
        }
    }


}
