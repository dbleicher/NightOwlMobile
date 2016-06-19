package com.geofinity.wgu.nightowl.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.MailTo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.geofinity.pwnet.events.DownloaderEvent;
import com.geofinity.pwnet.netops.ReqDownloadFile;
import com.geofinity.pwnet.netops.ReqPanopto;
import com.geofinity.pwnet.netops.ReqUnreadEmailWGU;
import com.geofinity.wgu.nightowl.NOMApp;
import com.geofinity.wgu.nightowl.R;
import com.geofinity.wgu.nightowl.model.PocketPreferences;

import java.io.File;
import java.net.HttpCookie;
import java.net.URLConnection;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by davidbleicher on 10/12/14.
 *
 * Done (b213):  Accept URL's from implicit intents
 *
 */
public class ActSecWeb extends ActionBarActivity {

    private Context ac;
    private PocketPreferences prefs;
    private WebView wvWebView;
    private LinearLayout pbcSpin;
    private EventBus eBus;

    private String targetURL;
    private String targetTitle;

    /**
     * Called when the activity is first created.
     */
    @SuppressLint({"SetJavaScriptEnabled"})
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        eBus = EventBus.getDefault();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.act_secweb);

        ac = getApplicationContext();
        prefs = NOMApp.prefs;


        ///////////////////////////////////////////
        //  Get the URI from an implicit Intent  //
        ///////////////////////////////////////////
        try {
            targetURL = getIntent().getDataString();
            targetTitle = "PocketWGU Web";
        } catch (Exception e) {
            // No problem, try the Intent extras
        }

        //////////////////////////////////////////
        //  Get the URL out of the bundle    //
        //////////////////////////////////////////
        Bundle myExtras = getIntent().getExtras();
        if (myExtras != null) {
            targetURL = myExtras.getString("TARGET_URL");
            targetTitle = myExtras.getString("TARGET_TITLE");
        }

        // Missing Title?
        if (targetTitle == null || targetTitle.equals("")) targetTitle = "Pocket Web";

        // Something went wrong!
        if (targetURL == null) {
            finish();
        }

        getSupportActionBar().setTitle(targetTitle);
        pbcSpin = (LinearLayout) findViewById(R.id.pbcSpin);

        // Make the spinner gold
        ProgressBar pbcSpin2 = (ProgressBar) findViewById(R.id.pbcSpin2);
        pbcSpin2.getIndeterminateDrawable().setColorFilter(0xFFF2C216, PorterDuff.Mode.SRC_IN);


        //////////////////////////
        //  Setup the WebView   //
        //////////////////////////
        wvWebView = (WebView) findViewById(R.id.wvWebView);

        WebSettings settings = wvWebView.getSettings();

        // settings.setUserAgentString(prefs.UA_GAL5_LOLLI);

        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);  //Use zoom capability
        settings.setDisplayZoomControls(false); //Don't display the controls

        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        // Enable everything
        settings.setDomStorageEnabled(true);
        settings.setAppCacheEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setGeolocationEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            settings.setAllowUniversalAccessFromFileURLs(true);
        }

        wvWebView.setWebChromeClient(new WebChromeClient());
        wvWebView.setWebViewClient(new MyWebViewClient());

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
            cookieManager.setAcceptThirdPartyCookies(wvWebView, true);
        }
        cookieManager.setCookie("wgu.edu", cookieString);

        wvWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {

                String cookieString = CookieManager.getInstance().getCookie(url);

                // It's ok if cookieString is null, downloader checks for that
                // Log.e("DOWNCOOKIE", cookieString);
                NOMApp.opEx.add(new ReqDownloadFile(url, Uri.parse(url).getLastPathSegment(), cookieString));
            }

        });

        // Good to go!
        if (targetURL != null && targetURL.startsWith("campusNews")) {
            wvWebView.loadDataWithBaseURL(
                    "https://webapp51.wgu.edu/",
                    prefs.getCampusNews(),
                    "text/html",
                    "UTF-8",
                    null);
        } else if (targetURL != null) {
            wvWebView.loadUrl(targetURL);
        } else {
            finish();
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
                prefs.customToast(this, "File Downloaded to \"My Saved Files\"", Toast.LENGTH_LONG);
                launchFileViewer(event.pathToFile);
                pbcSpin.setVisibility(View.GONE);
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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                NOMApp.opEx.add(new ReqUnreadEmailWGU());
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private class MyWebViewClient extends WebViewClient {
//        boolean scaleChangedRunnablePending = false;
//
//        @Override
//        public void onScaleChanged(WebView view, float oldScale, float newScale) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                if (scaleChangedRunnablePending) return;
//                view.postDelayed(new Runnable() {
//                    @TargetApi(Build.VERSION_CODES.KITKAT)
//                    @Override
//                    public void run() {
//                        wvWebView.evaluateJavascript(
//                                "document.body.style.width = window.innerWidth+'px';",
//                                null
//                        );
//                        scaleChangedRunnablePending = false;
//                    }
//                }, 100);
//            } else {
//                super.onScaleChanged(view, oldScale, newScale);
//            }
//        }


        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // Log.e("SECWEB", "SOURL: " + url);

            if (MailTo.isMailTo(url)) {
                MailTo mt = MailTo.parse(url);
                prefs.composeEmail(ActSecWeb.this, mt.getTo(), false);
                return true;
            }

            if (url.contains(".mp4")) {
                Intent cvi = new Intent(ActSecWeb.this, ActVideoPlayer.class);
                cvi.putExtra("STANDARD_VIDURL", url);
                startActivity(cvi);
                return true;
            }

            if (url.contains("//prezi") || url.contains("adobeconnect") ) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
                return true;
            }

            if (url.contains("lynda.com") ) {

                    try {
                        int vc = getPackageManager().getPackageInfo("com.lynda.android.root", 0).versionCode;
                        Uri uri = Uri.parse("lynda.com://lynda.com");
                        startActivity(new Intent(Intent.ACTION_VIEW, uri));
                        return true;
                    } catch (Exception e) {
                        // Do Nothing
                        return false;
                    }
            }



            if (url.contains("wgu.hosted.panopto.com")) {

                // Link to Video Folder
                if (url.contains("folderID=")) {
                    try {
                        Intent cvi = new Intent(getApplicationContext(), ActVideoList.class);
                        String folderID = url.split("folderID=")[1];
                        prefs.setPanoptoFolderId(folderID);
                        prefs.setPanoSearchTerm("Videos");
                        startActivity(cvi);
                        NOMApp.opEx.add(new ReqPanopto(null, folderID, 24));
                    } catch (Exception e) {
                        Log.e("PANOVID", "Bad Folder Search: " + e.getMessage());
                    }
                    return true;
                }

                // Link to Video Session
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

//            if (url.toLowerCase().endsWith(".pdf") ) {
//                //Log.e("URLPDF", "URL: " + url);
//                NOMApp.opEx.add(new ReqDownloadFile(url, Uri.parse(url).getLastPathSegment(), null));
//                return true;
//            }

            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // Log.e("SECWEB", "Starting: "+url);
            pbcSpin.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {

            // If the url doesn't have "mail.google", it's loading a different site
            // probably from a link in an email. Stop swiper.
            // Loading Gmail has multiple redirects, don't stop swiper
            // until the last one finishes (contains "Inbox").

            // Log.e("SECWEB", "Finished: " + url);
            if (!url.contains("mail.google") || url.contains("Inbox")) {
                pbcSpin.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && wvWebView.canGoBack()
                && !wvWebView.getUrl().equals("about:blank")) {
            // System.out.println("Current URL" + wvCosWeb.getUrl());
            wvWebView.goBack();
            return true;

        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        wvWebView.stopLoading();
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
    
}
